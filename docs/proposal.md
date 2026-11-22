# Project Proposal: Smart Parking Lot

## 1. Project Overview

The **Smart Parking Lot** automates routine parking operations so drivers can find and use compatible spaces. The system manages availability, space assignment, reservations, parking sessions, time limits, fees, payments, and occupancy updates. When an error or unusual situation occurs, an administrator can investigate and manually correct the parking flow, such as changing a space's status or correcting a session.

The system is intended to reduce the time drivers spend searching for parking, improve the use of available spaces, and automate routine parking operations. For this project, the proposed solution is a software prototype. Since the initial version may not have physical sensors or automated gates, it can simulate their arrival, occupancy, and exit events. A live deployment can connect these workflows to sensors and gates.

## 2. Problem Statement

Many parking facilities rely on drivers to search for spaces and on staff to track parking activity manually. This can lead to crowded entrances, unnecessary circulation inside the facility, uneven use of spaces, disputes over parking duration, and slow or inconvenient payment. Poorly enforced rules can also result in improper parking and blocked spaces.

The project proposes a centralized system that displays compatible parking availability, automatically assigns spaces, tracks parking sessions, applies parking rules, and processes payments. It alerts an administrator when an exception needs human review.

## 3. Proposed Solutions

| Identified problem | Proposed system response |
|---|---|
| Crowded parking areas | Show current compatible-space availability and automatically assign an open space. |
| Limited or unclear parking time | Display parking duration and applicable time limits; notify drivers as their session approaches its limit. |
| Traffic congestion | Automatically direct drivers to available spaces and support reservations to reduce time spent searching. |
| Poor space utilization | Track occupancy by space and make unused or newly vacated spaces visible promptly. |
| Poor driver behavior | Automatically track parking sessions and time limits, issue alerts, and refer unresolved incidents to the administrator. |
| Outdated payment methods | Calculate fees automatically and provide digital payment and electronic receipts using Philippine pesos (₱). |

## 4. Project Objectives

1. Let drivers choose their vehicle type before checking parking availability.
2. Automatically show and assign spaces compatible with the selected vehicle type.
3. Allow the system to create and manage reservations when reservations are enabled.
4. Automatically track vehicle entry, assigned space, parking duration, and exit.
5. Calculate parking fees from the configured rates and session duration.
6. Support digital payment and issue receipts automatically.
7. Notify the administrator about exceptions and provide tools to manually correct the affected parking flow.
8. Reduce the time needed to find a space and improve occupancy visibility.

## 5. Intended Users

- **Drivers:** Find spaces, make or cancel reservations, review parking sessions, and pay fees.
- **Administrators:** Monitor system activity, investigate alerts, and manually correct parking assignments, space status, or session records when an exception occurs.
- **Attendants (optional):** Provide on-site help only when an automated process fails or a driver needs assistance.

## 6. Proposed Features

### Driver features

- View available, occupied, reserved, and out-of-service spaces, updated by the system.
- Select a vehicle type (such as car, motorcycle, or electric vehicle) before checking availability; show only compatible spaces.
- Filter spaces by level, location, or accessibility where applicable.
- Request a reservation for an available, compatible space; the system confirms and assigns it.
- View an automatically tracked parking session and its time limit.
- Receive an automatic reminder before a session expires.
- Pay an automatically calculated fee and view a receipt or transaction history.

### Parking operations features

- Detect or simulate vehicle entry and exit events and create or close parking sessions.
- Match vehicle type to compatible spaces and assign or release spaces automatically.
- Apply configured rates and time limits and update occupancy automatically.
- Detect rule violations or operational exceptions and notify the administrator.

### Administrator features

- Review alerts for unavailable spaces, failed transactions, reservation problems, or other exceptions.
- Inspect related parking sessions and incident records.
- Manually correct a parking assignment, update a space's availability status, or correct a session record when needed.
- Record the resolution or escalate issues the administrator cannot fix.
- View occupancy and activity summaries when investigating a reported problem.

## 7. Scope and Assumptions

### Included in the initial version

- A single parking facility with multiple zones or levels.
- User roles for drivers and administrators; attendant assistance is optional for exceptional cases.
- Space, reservation, parking-session, fee, and payment-record management.
- An interface that can be implemented as a desktop or web application.
- Simulated entry, exit, and occupancy events when physical sensors or gates are unavailable, so the prototype can demonstrate automated workflows.

### Outside the initial version

- Installation and maintenance of physical sensors, cameras, or barriers.
- Guaranteed real-time integration with payment providers or government systems.
- Automated license-plate recognition.
- Multi-facility enterprise management.

These items can be considered for future development once the prototype and its operating requirements are established. Parking spaces, rates, vehicle compatibility rules, and operating hours are initialized as system configuration. The system handles normal parking operations, while administrators can intervene manually to recover from exceptions or correct inaccurate records.

## 8. Object-Oriented Design

The system can be organized around the following classes:

- `User` â€” shared account details and authentication behavior.
- `Driver`, `Attendant`, and `Administrator` â€” user roles with role-specific actions.
- `ParkingLot` â€” facility details, zones, spaces, and capacity.
- `ParkingSpace` â€” space identifier, type, status, and current assignment.
- `Vehicle` â€” plate number, type, and associated driver.
- `Reservation` â€” reserved space, driver, time window, and reservation status.
- `ParkingSession` â€” vehicle, space, entry time, exit time, and session status.
- `Rate` â€” pricing rules used to calculate a session fee.
- `Payment` â€” amount, method, transaction status, and receipt information.
- `Incident` â€” a recorded parking-rule issue or operational event.

The design applies core OOP concepts: **encapsulation** to protect object state, **inheritance** for shared user behavior, **polymorphism** for role-specific actions, and **abstraction** to keep workflows separate from storage and payment details. Services such as `ReservationService`, `ParkingService`, and `PaymentService` can coordinate related operations.

## 9. Main Workflow

1. The driver selects a vehicle type; the system shows available compatible spaces.
2. If the driver requests a reservation, the system checks availability and assigns a compatible space.
3. When the vehicle arrives, the system verifies the reservation or assigns an available compatible space automatically.
4. The system starts the parking session and updates occupancy from the entry event.
5. The system tracks the session and sends a time-limit reminder when needed.
6. When the vehicle exits, the system calculates the fee and processes digital payment.
7. After payment is confirmed, the system issues a receipt, closes the session, and releases the space.
8. If the system detects a problem it cannot resolve, it sends an alert so an administrator can investigate and manually correct the affected parking flow.

## 10. Non-Functional Requirements

- **Usability:** Driver actions should be clear and require minimal effort; administrator alerts should make exceptions easy to investigate.
- **Reliability:** A space should not be assigned to two active reservations or sessions at once.
- **Security:** User accounts and payment records should be protected through access controls; sensitive payment credentials should not be stored by the prototype.
- **Performance:** Availability and session information should update promptly after a recorded transaction.
- **Maintainability:** Rates, time limits, and space types should be configurable rather than embedded in application logic.
- **Accessibility:** The interface should use readable text, clear status labels, and keyboard-accessible controls.

## 11. Success Measures

The prototype will be considered successful if it can:

- Display correct availability based on recorded space status.
- Filter available spaces to match the driver's selected vehicle type.
- Automatically assign a compatible, available space and prevent duplicate assignments.
- Update occupancy and parking sessions from entry and exit events.
- Prevent duplicate active assignments and overlapping reservations.
- Start and close parking sessions with accurate timestamps.
- Calculate fees consistently from the configured rate.
- Record payments and produce a receipt or payment reference.
- Provide administrators with a useful occupancy summary and activity history.
- Let administrators apply and record a manual correction when an exception prevents the automated flow from completing.

In a live pilot, the facility could additionally compare average time to find a space, entry congestion, space occupancy, and payment processing time before and after adoption.

## 12. Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Availability data becomes outdated | Update status on each simulated or detected entry and exit event; let administrators correct a space status when the recorded state is wrong; connect sensors in a future phase. |
| Drivers do not follow space assignments or rules | Show clear space assignments and send unresolved incidents to the administrator for review. |
| Payment integration is unavailable | Keep the initial prototype's payment workflow provider-independent and simulate transaction results for demonstration. |
| Reservations are not honored because of late arrivals | Apply preconfigured expiry and grace-period rules automatically, and alert the administrator if a reservation exception needs review. |
| Personal or transaction data is exposed | Limit access by user role and minimize stored sensitive information. |

## 13. Conclusion

The Smart Parking Lot provides a practical response to the problems of crowding, congestion, poor space utilization, unclear time limits, driver non-compliance, and outdated payment methods. By combining availability tracking, parking-session management, configurable rules, and digital payment records in one system, the project can improve both the driver experience and day-to-day facility operations. The proposed prototype also creates a foundation for later integration with parking sensors, gates, and payment providers.
