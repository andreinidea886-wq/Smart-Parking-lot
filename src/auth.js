/* Browser UI for Java-backed administrator sessions. */
(() => {
  const path = location.pathname.toLowerCase();
  const loginPage = path.endsWith('/login.html') || path === 'login.html';
  const adminPage = path.endsWith('/admin.html') || path === 'admin.html';
  if (!loginPage && !adminPage) return;

  const checkSession = async () => {
    const response = await fetch('/api/session', { credentials: 'same-origin', cache: 'no-store' });
    if (!response.ok) throw new Error('Could not validate the admin session. Make sure the Java server is running.');
    return (await response.json()).authenticated === true;
  };

  if (loginPage) {
    const form = document.querySelector('#loginForm');
    const error = document.querySelector('#loginError');
    checkSession().then(authenticated => {
      if (authenticated) location.replace('/admin.html');
    }).catch(() => {});

    form.addEventListener('submit', async event => {
      event.preventDefault();
      const button = form.querySelector('button[type="submit"]');
      button.disabled = true;
      button.textContent = 'Signing in...';
      error.textContent = '';
      try {
        const response = await fetch('/api/login', {
          method: 'POST',
          credentials: 'same-origin',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: new URLSearchParams(new FormData(form))
        });
        const result = await response.json().catch(() => ({}));
        if (!response.ok) throw new Error(result.error || 'Unable to sign in.');
        location.replace('/admin.html');
      } catch (reason) {
        error.textContent = reason instanceof TypeError
          ? 'Cannot reach the Java server. Start it with the instructions below, then reload this page.'
          : (reason.message || 'Unable to sign in.');
        button.disabled = false;
        button.textContent = 'Sign in to dashboard';
      }
    });
    return;
  }

  checkSession().then(authenticated => {
    if (!authenticated) {
      location.replace('/login.html');
      return;
    }
    document.body.classList.add('authenticated');
    document.querySelector('#logoutButton')?.addEventListener('click', async () => {
      await fetch('/api/logout', { method: 'POST', credentials: 'same-origin' });
      location.replace('/login.html');
    });
  }).catch(() => location.replace('/login.html'));
})();
