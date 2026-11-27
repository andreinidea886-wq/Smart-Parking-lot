# Smart Parking Lot

A browser-based parking prototype built with HTML, CSS, JavaScript, and a small Java HTTP server. Drivers can view compatible spaces, create reservations, check in and out, and review simulated payments. Administrators can sign in to review activity, edit facility settings, and resolve alerts.

## Requirements

- Java 11 or newer (a JDK is required to compile `Server.java`)
- A modern web browser

The Java server uses only JDK libraries; no external dependencies are required.

## Run locally

### Run the one-time console flow

Open PowerShell in the project folder and run:

```powershell
Set-Location .\src\java
javac *.java
java ConsoleMain
```

Choose **1** to enter a vehicle, select its type and a payment method, and complete one simulated parking session and payment. The demo uses PHP 50 per hour and charges at least one hour.

### Run the website

Open PowerShell in the project folder, enter `src\java`, and compile the Java files:

```powershell
Set-Location .\src\java
javac *.java
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

All displayed parking rates and payment amounts use Philippine pesos (₱).

## Prototype notes

- Parking records are stored in the browser's `localStorage`. They are specific to that browser profile and are not synchronized between devices.
- The Java server keeps admin sessions in memory; restarting the server ends active sessions.
- Payment outcomes and parking entry/exit events are simulated. The site does not connect to real payment providers or IoT hardware.
- The included demo credentials are for local demonstration. Use HTTPS, stronger credential management, persistent data storage, and a security review before any public deployment.

## Project files

- `src/html/`: driver and administrator pages
- `src/css/`: shared styling
- `src/js/`: browser interactions and login UI
- `src/java/`: Java model classes and local server
- `docs/proposal.md`: project proposal
- `diagrams/`: driver, staff, and class diagrams

## Folder structure

```text
Smart Parking Lot/
|-- src/
|   |-- html/   Website pages
|   |-- css/    Stylesheets
|   |-- js/     Browser scripts
|   `-- java/   Java classes and server
|-- diagrams/   Draw.io workflow and class diagrams
|-- docs/       Proposal and project documentation
|-- README.md   Setup and usage guide
```
