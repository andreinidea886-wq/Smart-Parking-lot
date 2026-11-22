(() => {
  const $ = selector => document.querySelector(selector);
  const levels = ['Level 1', 'Level 2', 'Level 3'];
  const makeDefaultSpaces = () => {
  const spaces = [];
  ['1A','1B','1C','1D','1E','1F','1G','1H','2A','2B','2C','2D','2E','2F','2G','2H','3A','3B','3C','3D','3E','3F','3G','3H'].forEach((id,i)=>spaces.push({id,level:levels[Math.floor(i/8)],type:i%8===2?'EV':i%8===5?'Accessible':i%5===3?'Motorcycle':'Standard',status:'Available'}));
  ['1J','1K','1L','1M','1N','1P','1Q','1R','2J','2K','2L','2M','2N','2P','2Q','2R','3J','3K','3L','3M','3N','3P','3Q','3R'].forEach((id,i)=>spaces.push({id,level:levels[Math.floor(i/8)],type:i%8===2?'EV':i%8===5?'Accessible':i%5===3?'Motorcycle':'Standard',status:'Occupied'}));
  ['1S','1T','2S','2T','3S'].forEach((id,i)=>spaces.push({id,level:levels[Math.floor(i/2)],type:'Standard',status:'Reserved'}));
  ['2U','3U','3V'].forEach(id=>spaces.push({id,level:id[0]==='2'?'Level 2':'Level 3',type:'Standard',status:'Out of service'}));
  return spaces;
  };
  const readState = () => { try { return JSON.parse(localStorage.getItem('parkside-state') || 'null') || {}; } catch { return {}; } };
  let state = readState();
  state.spaces ||= makeDefaultSpaces();
  state.reservations ||= [];
  state.sessions ||= [];
  state.payments ||= [];
  state.activities ||= [];
  function normalizeCurrencyRecords(data) {
    data.payments.forEach(payment => {
      const amount = Number(String(payment.amount ?? '').replace(/[^\d.-]/g, ''));
      if (Number.isFinite(amount)) payment.amount = `₱${amount.toFixed(2)}`;
    });
    data.activities.forEach(activity => {
      if (typeof activity.text === 'string') activity.text = activity.text.replace(/\$(?=\s*\d)/g, '₱');
    });
  }
  normalizeCurrencyRecords(state);
  const defaultSettings={rates:{standard:2.5,motorcycle:1.25,ev:3.5},maxSessionHours:8,reservationGraceMinutes:30,reservationsEnabled:true,expiryReminders:true};
  state.settings={...defaultSettings,...state.settings,rates:{...defaultSettings.rates,...(state.settings?.rates||{})}};
  let levelFilter = 'All';
  let chosenVehicle = $('#vehicleType').value;
  let pendingSpace = null;
  let checkoutSpace = null;
  let paymentFailed = false;

  const isCompatible = (space, vehicle) => vehicle === 'EV' ? space.type === 'EV'
  : vehicle === 'Accessible' ? space.type === 'Accessible'
  : vehicle === 'Motorcycle' ? ['Motorcycle','Standard'].includes(space.type)
  : ['Standard','Accessible'].includes(space.type);
  const availableCount = () => state.spaces.filter(space => space.status === 'Available').length;
  const rateFor = vehicle => vehicle === 'Motorcycle' ? Number(state.settings.rates.motorcycle) : vehicle === 'EV' ? Number(state.settings.rates.ev) : Number(state.settings.rates.standard);
  const fmtMoney = amount => '₱' + amount.toFixed(2);
  const elapsedMs = session => Math.max(0, Date.now() - new Date(session.startedAt || Date.now()).getTime());
  const elapsedLabel = ms => { const mins=Math.floor(ms/60000),hours=Math.floor(mins/60);return hours?`${hours}h ${mins%60}m`:`${mins}m`; };
  const feeFor = session => Math.max(1, Math.ceil(elapsedMs(session)/3600000)) * rateFor(session.vehicle);
  const save = () => { try { localStorage.setItem('parkside-state', JSON.stringify(state)); } catch {} };
  function checkTimeReminder(){
  state.sessions.forEach(session=>{
  const reminderAt=Math.max(.5,Number(state.settings.maxSessionHours)-.5)*60*60*1000;
  if(state.settings.expiryReminders&&!session.reminderSent&&elapsedMs(session)>=reminderAt){
  session.reminderSent=true;
  state.activities.unshift({icon:'alert',text:`<strong>${session.driver}</strong> is 30 minutes from the ${state.settings.maxSessionHours} hour parking limit`,time:'Just now'});
  save();
  if(state.driverProfile?.plate===session.plate)showToast('Parking time limit reminder: 30 minutes remaining.');
  }
  });
  }
  function expireReservations(){let changed=false;state.reservations.forEach(reservation=>{if(reservation.status==='Reserved'&&reservation.expiresAt&&Date.now()>new Date(reservation.expiresAt).getTime()){reservation.status='Expired';const space=state.spaces.find(item=>item.id===reservation.space);if(space?.status==='Reserved')space.status='Available';state.incidents||=[];state.incidents.unshift({id:'INC-'+Date.now()+'-'+reservation.space,type:'Reservation expired',details:`Reservation ${reservation.reference} expired before driver check-in.`,space:reservation.space,status:'Open',createdAt:new Date().toISOString()});changed=true;}});if(changed){save();render();}}
  function cancelReservation(reference){const reservation=state.reservations.find(item=>item.reference===reference);if(!reservation||reservation.status!=='Reserved')return;reservation.status='Cancelled';reservation.cancelledAt=new Date().toISOString();const space=state.spaces.find(item=>item.id===reservation.space);if(space?.status==='Reserved')space.status='Available';state.activities.unshift({icon:'cal',text:`<strong>${reservation.driver}</strong> cancelled reservation ${reservation.reference}`,time:'Just now'});save();render();showToast('Reservation cancelled. The space is available again.');}

  function renderSpaces() {
  const matches=state.spaces.filter(space=>space.status==='Available'&&isCompatible(space,chosenVehicle)).filter(space=>levelFilter==='All'||space.level===levelFilter);
  $('#heroAvailable').textContent=availableCount();
  $('#resultCount').textContent=matches.length;
  $('#baseRate').textContent=`${fmtMoney(rateFor(chosenVehicle))} / hour`;
  $('#driverSpaceGrid').innerHTML=matches.map(space=>{
  const price=rateFor(space.type==='Standard'?chosenVehicle:space.type);
  const kind=space.type==='EV'?'EV charging':space.type==='Accessible'?'Accessible':space.type;
  return `<article class="driver-space-card"><div class="driver-space-top"><span class="driver-space-type">${kind}</span><span class="driver-open-pill"><i></i>Available</span></div><div class="driver-space-id">${space.id}</div><div class="driver-space-location">${space.level} <span>  </span> Near ${space.level==='Level 1'?'main entrance':space.level==='Level 2'?'elevator':'north exit'}</div><div class="driver-space-bottom"><span class="driver-space-price">${fmtMoney(price)}<small> / hour</small></span><button class="reserve-space-btn" data-space="${space.id}">Reserve this spot <span>  </span></button></div></article>`;
  }).join('');
  $('#noSpaces').hidden=matches.length>0;
  $('#driverSpaceGrid').hidden=matches.length===0;
  document.querySelectorAll('[data-space]').forEach(button=>button.addEventListener('click',()=>openReserve(button.dataset.space)));
  }
  function renderVisit(){
  const profile=state.driverProfile;
  if(!profile){$('#myVisit').hidden=true;$('#driverPaymentHistory').hidden=true;return;}
  const session=state.sessions.find(item=>item.driver===profile.name&&item.plate===profile.plate);
  const reservation=state.reservations.find(item=>item.reference===profile.reference);
  $('#myVisit').hidden=false;
  if(session){
  $('#visitHeading').textContent=`Your car is parked in ${session.space}`;
  $('#visitDescription').textContent=`${profile.plate} - ${session.vehicle==='EV'?'Electric vehicle':session.vehicle} - ${session.reminderSent?'30 minute time-limit reminder sent':`Time limit: ${state.settings.maxSessionHours} hours`}`;
  $('#visitDuration').textContent=elapsedLabel(elapsedMs(session));$('#visitFee').textContent=fmtMoney(feeFor(session));
  $('#driverCheckout').hidden=false;$('#driverCheckout').textContent='End parking & pay';$('#driverCheckout').onclick=()=>openPayment(session.space);$('#cancelMyReservation').hidden=true;
  }else if(reservation?.status==='Reserved'){
  $('#visitHeading').textContent=`Space ${reservation.space} is saved for you`;$('#visitDescription').textContent=`${reservation.arrival} - ${reservation.duration} - ${reservation.plate}`;
  $('#visitDuration').textContent='Not started';$('#visitFee').textContent='-';$('#driverCheckout').hidden=false;$('#driverCheckout').textContent='I have arrived - start parking';$('#driverCheckout').onclick=()=>checkInReservation(reservation);
  $('#cancelMyReservation').hidden=false;$('#cancelMyReservation').onclick=()=>cancelReservation(reservation.reference);
  }else if(reservation&&['Expired','Cancelled'].includes(reservation.status)){
  $('#visitHeading').textContent=reservation.status==='Expired'?'Your reservation expired':'Reservation cancelled';$('#visitDescription').textContent='This space is available again. Search for a new reservation whenever you are ready.';
  $('#visitDuration').textContent='Not started';$('#visitFee').textContent='-';$('#driverCheckout').hidden=true;$('#cancelMyReservation').hidden=true;
  }else if(state.lastReceipt?.driver===profile.name&&state.lastReceipt?.plate===profile.plate){
  $('#visitHeading').textContent='Thanks for parking with us';$('#visitDescription').textContent=`Payment complete - Receipt ${state.lastReceipt.receipt}`;
  $('#visitDuration').textContent=state.lastReceipt.duration;$('#visitFee').textContent=state.lastReceipt.amount;$('#driverCheckout').hidden=true;$('#cancelMyReservation').hidden=true;
  }else{$('#myVisit').hidden=true;$('#cancelMyReservation').hidden=true;}
  const receipts=state.payments.filter(payment=>payment.driver===profile.name||payment.plate===profile.plate).slice(0,5);
  $('#driverPaymentHistory').hidden=receipts.length===0;
  $('#driverReceiptList').innerHTML=receipts.map(payment=>`<div class="driver-receipt-row"><span>${payment.receipt} - ${payment.method} - ${payment.duration}</span><strong>${payment.amount}</strong></div>`).join('');
  }  function render(){renderSpaces();renderVisit();}
  function openReserve(spaceId){
  if(!state.settings.reservationsEnabled){showToast('Reservations are currently disabled.');return;}
  const space=state.spaces.find(item=>item.id===spaceId);
  if(!space||space.status!=='Available'||!isCompatible(space,chosenVehicle)){showToast('That spot is no longer available. Choose another.');render();return;}
  pendingSpace=space.id;
  $('#ticketSpace').textContent=space.id;
  $('#ticketArrival').textContent=$('#arrivalTime').value;
  $('#ticketVehicle').textContent=chosenVehicle==='EV'?'Electric vehicle':chosenVehicle;
  $('#reserveDetails').hidden=false;$('#ticketArea').hidden=true;$('#driverCheckin').hidden=true;
  $('#reserveKicker').textContent='READY WHEN YOU ARE';$('#reserveTitle').textContent='Confirm your reservation';
  $('#reserveCopy').textContent=`Add your details to reserve space ${space.id} for ${$('#arrivalTime').value.toLowerCase()}.`;
  $('#doneReserve').textContent='Confirm reservation';
  $('#reserveModal').classList.add('open');$('#reserveModal').setAttribute('aria-hidden','false');$('#driverName').focus();
  }
  function confirmReservation(){
  const space=state.spaces.find(item=>item.id===pendingSpace);
  if(!space||space.status!=='Available'||!isCompatible(space,chosenVehicle)){showToast('That spot is no longer available. Choose another.');closeReserve();render();return;}
  const driver=$('#driverName').value.trim()||'Guest Driver';
  const plate=$('#driverPlate').value.trim().toUpperCase()||'  ';
  const reference='PS-'+Math.random().toString(36).slice(2,7).toUpperCase();
  space.status='Reserved';
  const createdAt=Date.now(),arrivalChoice=$('#arrivalTime').value,offset=arrivalChoice==='In 30 minutes'?30*60000:arrivalChoice==='In 1 hour'?60*60000:arrivalChoice==='In 2 hours'?120*60000:0,arrivalAt=createdAt+offset;
  state.reservations.unshift({driver,plate,vehicle:chosenVehicle,space:space.id,arrival:arrivalChoice,duration:$('#stayDuration').value,status:'Reserved',reference,createdAt:new Date(createdAt).toISOString(),arrivalAt:new Date(arrivalAt).toISOString(),expiresAt:new Date(arrivalAt+Number(state.settings.reservationGraceMinutes)*60000).toISOString()});
  state.driverProfile={name:driver,plate,reference};
  state.activities.unshift({icon:'cal',text:`<strong>${driver}</strong> reserved space ${space.id}`,time:'Just now'});
  save();$('#ticketRef').textContent=reference;$('#reserveDetails').hidden=true;$('#ticketArea').hidden=false;
  $('#reserveKicker').textContent='YOU  RE ALL SET';$('#reserveTitle').textContent='Your spot is reserved';
  $('#reserveCopy').textContent='When you reach the facility, check in here to start your parking session.';
  $('#doneReserve').textContent='Got it';$('#driverCheckin').hidden=false;pendingSpace=null;render();
  }
  function checkInReservation(reservation){
  const current=state.reservations.find(item=>item.reference===reservation.reference);
  if(!current||current.status!=='Reserved'||(current.expiresAt&&Date.now()>new Date(current.expiresAt).getTime())){expireReservations();showToast('This reservation has expired or is no longer active.');return;}
  if(current.arrivalAt&&Date.now()<new Date(current.arrivalAt).getTime()){showToast('Your reservation arrival window has not started yet.');return;}
  const space=state.spaces.find(item=>item.id===reservation.space);
  if(!current||current.status!=='Reserved'||!space||space.status!=='Reserved'||!isCompatible(space,current.vehicle)){
  state.incidents||=[];state.incidents.unshift({id:'INC-'+Date.now(),type:'Reservation exception',details:`Reservation ${reservation.reference} could not be verified against space ${reservation.space}.`,space:reservation.space,status:'Open',createdAt:new Date().toISOString()});
  state.alertOpen=true;save();showToast('Reservation could not be verified. An administrator has been notified.');return;
  }
  current.status='Checked in';current.checkedInAt=new Date().toISOString();space.status='Occupied';
  state.sessions.unshift({driver:current.driver,plate:current.plate,vehicle:current.vehicle,space:current.space,started:'Just now',startedAt:current.checkedInAt,duration:'0m',status:'Active',reservationReference:current.reference});
  state.activities.unshift({icon:'car',text:`<strong>${current.driver}</strong> checked in at space ${space.id}`,time:'Just now'});
  save();$('#reserveModal').classList.remove('open');$('#reserveModal').setAttribute('aria-hidden','true');render();showToast(`Reservation verified. Session started at ${space.id}.`);
  }
  function openPayment(spaceId){
  const session=state.sessions.find(item=>item.space===spaceId);if(!session)return;
  checkoutSpace=spaceId;paymentFailed=false;
  $('#driverPaymentSummary').textContent=`${elapsedLabel(elapsedMs(session))} parked  ${session.space}  amount due ${fmtMoney(feeFor(session))}`;
  $('#driverPaymentError').hidden=true;$('#completeDriverPayment').textContent='Pay now';
  $('#driverPaymentModal').classList.add('open');$('#driverPaymentModal').setAttribute('aria-hidden','false');
  }
  function simulateFailure(){paymentFailed=true;const session=state.sessions.find(item=>item.space===checkoutSpace);if(session&&!session.paymentIncidentId){state.incidents||=[];session.paymentIncidentId='INC-'+Date.now();state.incidents.unshift({id:session.paymentIncidentId,type:'Failed payment',details:`Payment attempt for ${session.driver} was declined. The active session remains open.`,space:session.space,status:'Open',createdAt:new Date().toISOString()});save();}$('#driverPaymentError').textContent='Payment was declined. Your session is still active and your space remains occupied. Choose retry to attempt payment again.';$('#driverPaymentError').hidden=false;$('#completeDriverPayment').textContent='Retry payment';}
  function completePayment(){
  if(paymentFailed){paymentFailed=false;$('#driverPaymentError').textContent='Retry successful. Your payment has been recorded.';$('#driverPaymentError').hidden=false;setTimeout(()=>finishCheckout(),550);return;}
  finishCheckout();
  }
  function finishCheckout(){
  const index=state.sessions.findIndex(item=>item.space===checkoutSpace);if(index<0)return;
  const session=state.sessions[index],amount=feeFor(session),duration=elapsedLabel(elapsedMs(session));
  const receipt='RCP-'+(20482+state.payments.length);
  const payment={receipt,driver:session.driver,space:session.space,duration,method:$('#driverPaymentMethod').value,amount:fmtMoney(amount),status:'Paid',paidAt:new Date().toISOString()};
  state.payments.unshift(payment);state.sessions.splice(index,1);
  if(session.paymentIncidentId){const incident=state.incidents?.find(item=>item.id===session.paymentIncidentId);if(incident){incident.status='Resolved';incident.resolution='Payment succeeded after retry.';incident.resolvedAt=new Date().toISOString();}}
  const space=state.spaces.find(item=>item.id===session.space);if(space)space.status='Available';
  const reservation=state.reservations.find(item=>item.reference===session.reservationReference);if(reservation)reservation.status='Completed';
  state.lastReceipt={...payment,plate:session.plate};
  state.activities.unshift({icon:'pay',text:`<strong>${session.driver}</strong> paid ${payment.amount} via ${payment.method}  ${receipt}`,time:'Just now'});
  save();checkoutSpace=null;$('#driverPaymentModal').classList.remove('open');$('#driverPaymentModal').setAttribute('aria-hidden','true');render();showToast(`Payment complete. Receipt ${receipt} issued.`);
  }
  function showToast(message){const node=$('#driverToast');node.textContent=message;node.classList.add('show');clearTimeout(window.driverToastTimer);window.driverToastTimer=setTimeout(()=>node.classList.remove('show'),3000);}
  function closeReserve(){pendingSpace=null;$('#reserveModal').classList.remove('open');$('#reserveModal').setAttribute('aria-hidden','true');}
  function closePayment(){checkoutSpace=null;$('#driverPaymentModal').classList.remove('open');$('#driverPaymentModal').setAttribute('aria-hidden','true');}

  $('#findForm').addEventListener('submit',event=>{event.preventDefault();chosenVehicle=$('#vehicleType').value;renderSpaces();$('#results').scrollIntoView({behavior:'smooth',block:'start'});});
  $('#vehicleType').addEventListener('change',()=>{chosenVehicle=$('#vehicleType').value;renderSpaces();});
  document.querySelectorAll('[data-filter-level]').forEach(button=>button.addEventListener('click',()=>{document.querySelectorAll('[data-filter-level]').forEach(item=>item.classList.toggle('selected',item===button));levelFilter=button.dataset.filterLevel;renderSpaces();}));
  $('#resetFilters').addEventListener('click',()=>{levelFilter='All';document.querySelectorAll('[data-filter-level]').forEach(button=>button.classList.toggle('selected',button.dataset.filterLevel==='All'));renderSpaces();});
  $('#refreshAvailability').addEventListener('click',()=>{state={...state,...readState()};state.spaces||=makeDefaultSpaces();state.reservations||=[];state.sessions||=[];state.payments||=[];state.activities||=[];normalizeCurrencyRecords(state);render();showToast('Compatible space availability refreshed.');});
  $('#doneReserve').addEventListener('click',()=>pendingSpace?confirmReservation():closeReserve());
  $('#driverCheckin').addEventListener('click',()=>{const reservation=state.reservations.find(item=>item.reference===state.driverProfile?.reference);if(reservation)checkInReservation(reservation);});
  $('#completeDriverPayment').addEventListener('click',completePayment);$('#simulatePaymentFailure').addEventListener('click',simulateFailure);
  $('#driverPaymentClose').addEventListener('click',closePayment);$('#driverPaymentModal').addEventListener('click',event=>{if(event.target===$('#driverPaymentModal'))closePayment();});
  $('#reserveClose').addEventListener('click',closeReserve);$('#reserveModal').addEventListener('click',event=>{if(event.target===$('#reserveModal'))closeReserve();});
  document.addEventListener('keydown',event=>{if(event.key==='Escape'){closeReserve();closePayment();}});
  window.addEventListener('storage',event=>{if(event.key!=='parkside-state'||!event.newValue)return;try{state=JSON.parse(event.newValue);state.spaces||=makeDefaultSpaces();state.reservations||=[];state.sessions||=[];state.payments||=[];state.activities||=[];normalizeCurrencyRecords(state);render();}catch{}});
  render();
  setInterval(()=>{expireReservations();checkTimeReminder();if(state.driverProfile)renderVisit();},30000);
})();
