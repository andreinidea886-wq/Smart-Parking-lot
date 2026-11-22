# Smart Parking Lot

A browser-based parking prototype built with HTML, CSS, JavaScript, and a small Java HTTP server. Drivers can view compatible spaces, create reservations, check in and out, and review simulated payments. Administrators can sign in to review activity, edit facility settings, and resolve alerts.

## Requirements

- Java 11 or newer (a JDK is required to compile `Server.java`)
- A modern web browser

The Java server uses only JDK libraries; no external dependencies are required.

## Run locally

Open PowerShell in the project folder and compile the server:

```powershell
javac Server.java
```

Start the server:

```powershell
java Server
```

Keep that terminal open while using the website. Open these addresses in your browser:

- Driver website: <http://localhost:8080>
- Admin login: <http://localhost:8080/login.html>

To stop the server, press **Ctrl+C** in the terminal.

## Admin demo login

- Username: `admin`
- Password: `admin123`

You can set different demo credentials with the `PARKSIDE_ADMIN_USER` and `PARKSIDE_ADMIN_PASSWORD` environment variables before starting Java. For example, in PowerShell:

```powershell
$env:PARKSIDE_ADMIN_USER = "facilityadmin"
$env:PARKSIDE_ADMIN_PASSWORD = "choose-a-demo-password"
java Server
```

If port `8080` is already in use, set a different port before starting the server:

```powershell
$env:PORT = "8081"
java Server
```

Then visit `http://localhost:8081`.

## Features

- Filter available spaces by vehicle type.
- Create and manage reservations.
- Simulate vehicle check-in, parking sessions, fees, and checkout.
- Select Maya, PayPal, GCash, GoMo, cash, or another e-wallet for simulated payment records.
- Use the admin dashboard to review alerts, update settings, and manage parking activity.

## Prototype notes

- Parking records are stored in the browser's `localStorage`. They are specific to that browser profile and are not synchronized between devices.
- The Java server keeps admin sessions in memory; restarting the server ends active sessions.
- Payment outcomes and parking entry/exit events are simulated. The site does not connect to real payment providers or IoT hardware.
- The included demo credentials are for local demonstration. Use HTTPS, stronger credential management, persistent data storage, and a security review before any public deployment.

## Project files

- `index.html`, `admin.html`, `login.html`: driver and administrator pages
- `styles.css`: shared styling
- `driver.js`, `auth.js`: browser interactions and login UI
- `Server.java`: local Java server, protected admin page, and login/session endpoints
- `proposal.md`: project proposal
- `SPLS.drawio`, `SPLS_Staff_Flowcharts.drawio`, `SPLS_Class_Diagram.drawio`: workflow and class diagrams
