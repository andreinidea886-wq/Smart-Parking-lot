import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Small dependency-free checks for the parking domain classes. */
public final class CoreClassesTest {
    private static int checks;

    private CoreClassesTest() { }

    public static void main(String[] args) {
        testValidation();
        testCompatibilityPolicy();
        testVehicle();
        testParkingSpace();
        testParkingLot();
        testParkingServiceAndSession();
        testDriver();
        testAdministratorAndIncident();
        testAttendant();
        testReservationAndService();
        testRate();
        testPaymentAndService();
        testEnums();
        System.out.println("Passed " + checks + " checks.");
    }

    private static void testValidation() {
        check("text".equals(Validation.requireText(" text ", "value")), "text is trimmed");
        expectThrows(IllegalArgumentException.class,
            () -> Validation.requireText("  ", "value"));
        expectThrows(IllegalArgumentException.class,
            () -> Validation.requireNonNull(null, "value"));
        expectThrows(IllegalArgumentException.class,
            () -> Validation.requirePositive(0, "value"));
        expectThrows(IllegalArgumentException.class,
            () -> Validation.requireNotFuture(Instant.now().plusSeconds(60), "time"));
    }

    private static void testCompatibilityPolicy() {
        check(CompatibilityPolicy.isCompatible(VehicleType.STANDARD, SpaceType.STANDARD),
            "standard vehicle uses standard space");
        check(CompatibilityPolicy.isCompatible(VehicleType.STANDARD, SpaceType.ACCESSIBLE),
            "standard vehicle may use accessible space");
        check(!CompatibilityPolicy.isCompatible(VehicleType.EV, SpaceType.STANDARD),
            "EV cannot use standard space");
        expectThrows(IllegalArgumentException.class,
            () -> CompatibilityPolicy.isCompatible(null, SpaceType.STANDARD));
    }

    private static void testVehicle() {
        Vehicle vehicle = new Vehicle("ABC-123", VehicleType.STANDARD);
        check("ABC-123".equals(vehicle.getPlate()), "vehicle plate is stored");
        check(vehicle.isCompatible(new ParkingSpace("S-1", "L1", SpaceType.STANDARD)),
            "vehicle reports compatible space");
        check(!vehicle.isCompatible(null), "null space is incompatible");
        expectThrows(IllegalArgumentException.class,
            () -> new Vehicle(" ", VehicleType.STANDARD));
        expectThrows(IllegalArgumentException.class,
            () -> new Vehicle("ABC-123", null));
    }

    private static void testParkingSpace() {
        ParkingSpace space = new ParkingSpace("S-1", "L1", SpaceType.STANDARD);
        check(space.getStatus() == SpaceStatus.AVAILABLE, "new space is available");
        space.reserve();
        check(space.getStatus() == SpaceStatus.RESERVED, "reserve changes space status");
        space.occupy();
        space.release();
        check(space.getStatus() == SpaceStatus.AVAILABLE, "release makes space available");
        space.setOutOfService();
        expectThrows(IllegalStateException.class, space::release);
        expectThrows(IllegalArgumentException.class,
            () -> new ParkingSpace("S-2", "L1", SpaceType.STANDARD, null));
    }

    private static void testParkingLot() {
        ParkingSpace standard = new ParkingSpace("S-1", "L1", SpaceType.STANDARD);
        ParkingLot lot = new ParkingLot("Main", Arrays.asList(standard));
        check(lot.getCapacity() == 1, "lot capacity is number of spaces");
        check(lot.availableSpaces(VehicleType.STANDARD).size() == 1,
            "lot finds compatible available space");
        check(lot.assignSpace(new Vehicle("ABC-123", VehicleType.STANDARD)) == standard,
            "lot assigns compatible space");
        check(lot.getOccupiedCount() == 0, "reserved space is not counted as occupied");
        expectThrows(IllegalArgumentException.class,
            () -> new ParkingLot("Empty", List.of()));
        expectThrows(IllegalArgumentException.class,
            () -> new ParkingLot("Duplicate", Arrays.asList(
                new ParkingSpace("D-1", "L1", SpaceType.STANDARD),
                new ParkingSpace("D-1", "L2", SpaceType.EV))));
    }

    private static void testParkingServiceAndSession() {
        ParkingSpace space = new ParkingSpace("S-1", "L1", SpaceType.STANDARD);
        ParkingLot lot = new ParkingLot("Main", List.of(space));
        ParkingService service = new ParkingService(lot, 24);
        ParkingSession session = service.startSession(new Vehicle("ABC-123", VehicleType.STANDARD));
        check(session.getStatus() == SessionStatus.ACTIVE, "started session is active");
        check(space.getStatus() == SpaceStatus.OCCUPIED, "starting session occupies space");
        check(!session.duration().isNegative(), "session duration is non-negative");
        service.endSession(session);
        check(session.getStatus() == SessionStatus.COMPLETED, "ended session is completed");
        check(space.getStatus() == SpaceStatus.AVAILABLE, "ending session releases space");
        expectThrows(IllegalStateException.class, () -> service.endSession(session));
        expectThrows(IllegalArgumentException.class,
            () -> new ParkingService(lot, 0));
        expectThrows(IllegalArgumentException.class,
            () -> service.startSession(new Vehicle("EV-1", VehicleType.EV), space));
    }

    private static void testDriver() {
        Driver driver = new Driver("D-1", "Ari", "ari@example.test");
        Vehicle vehicle = new Vehicle("ABC-123", VehicleType.STANDARD);
        check("D-1".equals(driver.getId()), "driver inherits user identity");
        driver.addVehicle(vehicle);
        check(driver.getVehicles().size() == 1, "driver registers a vehicle");
        expectThrows(IllegalArgumentException.class, () -> driver.addVehicle(vehicle));
        driver.removeVehicle(vehicle);
        check(driver.getVehicles().isEmpty(), "driver removes a registered vehicle");
        expectThrows(IllegalArgumentException.class, () -> driver.removeVehicle(vehicle));
    }

    private static void testAdministratorAndIncident() {
        Administrator admin = new Administrator("A-1", "Sam", "sam@example.test", "Manager");
        check("Manager".equals(admin.getRole()), "administrator role is stored");
        ParkingSpace space = new ParkingSpace("S-1", "L1", SpaceType.STANDARD);
        admin.updateSpace(space, SpaceStatus.OUT_OF_SERVICE);
        check(space.getStatus() == SpaceStatus.OUT_OF_SERVICE, "administrator updates a space");

        Incident incident = new Incident("I-1", "Damage", "Broken sign", space, Instant.now());
        check(incident.getStatus() == IncidentStatus.OPEN, "new incident is open");
        admin.resolveIncident(incident, "Sign replaced");
        check(incident.getStatus() == IncidentStatus.RESOLVED, "administrator resolves incident");
        check("Sign replaced".equals(incident.getResolution()), "incident records resolution note");
        expectThrows(IllegalStateException.class, () -> incident.resolve("Again"));
        expectThrows(IllegalArgumentException.class,
            () -> incident.resolve(" "));
        expectThrows(IllegalArgumentException.class,
            () -> new Incident("I-2", "Damage", "Details", null, Instant.now().plusSeconds(60)));
    }

    private static void testAttendant() {
        ParkingLot lot = new ParkingLot("Main", List.of(
            new ParkingSpace("S-1", "L1", SpaceType.STANDARD)));
        ParkingService service = new ParkingService(lot, 24);
        Attendant attendant = new Attendant("AT-1", "Lee", "lee@example.test", service);
        ParkingSession session = attendant.recordEntry(new Vehicle("ABC-123", VehicleType.STANDARD));
        check(attendant.getAssignedSessions().contains(session), "attendant records entry");
        expectThrows(IllegalStateException.class,
            () -> attendant.recordEntry(new Vehicle("ABC-123", VehicleType.STANDARD)));
        attendant.recordExit(session);
        check(attendant.getAssignedSessions().isEmpty(), "attendant records exit");
        expectThrows(IllegalArgumentException.class, () -> attendant.recordExit(session));
    }

    private static void testReservationAndService() {
        Driver driver = new Driver("D-1", "Ari", "ari@example.test");
        Vehicle vehicle = new Vehicle("ABC-123", VehicleType.STANDARD);
        driver.addVehicle(vehicle);
        ParkingLot lot = new ParkingLot("Main", List.of(
            new ParkingSpace("S-1", "L1", SpaceType.STANDARD)));
        ReservationService service = new ReservationService(lot, Duration.ofHours(1));
        Reservation reservation = service.reserve(driver, vehicle);
        check(reservation.getStatus() == ReservationStatus.RESERVED,
            "service creates a reserved reservation");
        check(reservation.getSpace().getStatus() == SpaceStatus.RESERVED,
            "reservation holds its space");
        expectThrows(IllegalArgumentException.class,
            () -> service.reserve(new Driver("D-2", "Bo", "bo@example.test"), vehicle));
        reservation.cancel();
        check(reservation.getStatus() == ReservationStatus.CANCELLED,
            "reservation can be cancelled");
        check(reservation.getSpace().getStatus() == SpaceStatus.AVAILABLE,
            "cancelling releases reserved space");
        expectThrows(IllegalStateException.class, reservation::checkIn);
        expectThrows(IllegalArgumentException.class,
            () -> new ReservationService(lot, Duration.ZERO));
    }

    private static void testRate() {
        Rate rate = new Rate(VehicleType.STANDARD, BigDecimal.valueOf(50));
        check(rate.calculate(Duration.ZERO).compareTo(BigDecimal.valueOf(50)) == 0,
            "zero duration charges one hour");
        check(rate.calculate(Duration.ofSeconds(3601)).compareTo(BigDecimal.valueOf(100)) == 0,
            "partial hour rounds up");
        expectThrows(IllegalArgumentException.class,
            () -> new Rate(VehicleType.STANDARD, BigDecimal.valueOf(-1)));
        expectThrows(IllegalArgumentException.class,
            () -> rate.calculate(Duration.ofSeconds(-1)));
    }

    private static void testPaymentAndService() {
        Payment payment = new Payment("P-1", BigDecimal.valueOf(50), PaymentMethod.CASH);
        check(payment.getStatus() == PaymentStatus.PENDING, "new payment is pending");
        payment.recordResult(true, Instant.now());
        check(payment.getStatus() == PaymentStatus.PAID, "successful payment is paid");
        check(payment.getPaidAt() != null, "successful payment records paid time");
        check(payment.receipt().contains("P-1"), "receipt contains payment reference");
        expectThrows(IllegalStateException.class,
            () -> payment.recordResult(true, Instant.now()));
        expectThrows(IllegalArgumentException.class,
            () -> new Payment("P-2", BigDecimal.valueOf(-1), PaymentMethod.CASH));

        ParkingLot lot = new ParkingLot("Main", List.of(
            new ParkingSpace("S-1", "L1", SpaceType.STANDARD)));
        ParkingService parking = new ParkingService(lot, 24);
        ParkingSession session = parking.startSession(new Vehicle("XYZ-9", VehicleType.STANDARD));
        PaymentService payments = new PaymentService("Demo", Map.of(
            VehicleType.STANDARD, new Rate(VehicleType.STANDARD, BigDecimal.valueOf(50))));
        expectThrows(IllegalStateException.class,
            () -> payments.process(session, PaymentMethod.CASH));
        parking.endSession(session);
        Payment completedPayment = payments.process(session, PaymentMethod.CASH);
        check(completedPayment.getStatus() == PaymentStatus.PAID,
            "payment service pays for completed session");
        expectThrows(IllegalStateException.class,
            () -> payments.process(session, PaymentMethod.CASH));
        check(payments.issueReceipt(completedPayment).contains(completedPayment.getReference()),
            "payment service issues paid receipt");
    }

    private static void testEnums() {
        check(VehicleType.values().length == 4, "vehicle types are defined");
        check(SpaceType.values().length == 4, "space types are defined");
        check(PaymentMethod.values().length >= 1, "payment methods are defined");
        check(SpaceStatus.values().length == 4, "space statuses are defined");
        check(SessionStatus.values().length == 2, "session statuses are defined");
        check(PaymentStatus.values().length == 3, "payment statuses are defined");
        check(ReservationStatus.values().length == 5, "reservation statuses are defined");
        check(IncidentStatus.values().length == 2, "incident statuses are defined");
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
        checks++;
    }

    private static void expectThrows(Class<? extends Throwable> expected, Runnable action) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (expected.isInstance(actual)) {
                checks++;
                return;
            }
            throw new AssertionError("Expected " + expected.getSimpleName()
                + " but caught " + actual.getClass().getSimpleName(), actual);
        }
        throw new AssertionError("Expected " + expected.getSimpleName() + " but nothing was thrown");
    }
}
