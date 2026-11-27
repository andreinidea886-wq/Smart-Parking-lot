import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Coordinates reservation creation, cancellation, and expiry.
 */
public class ReservationService {
    private final List<Reservation> reservations;
    private final ParkingLot parkingLot;
    private final Duration reservationWindow;

    public ReservationService(ParkingLot parkingLot, Duration reservationWindow) {
        this.parkingLot = Validation.requireNonNull(parkingLot, "parkingLot");
        this.reservationWindow = Validation.requireNonNull(reservationWindow, "reservationWindow");
        if (reservationWindow.isNegative() || reservationWindow.isZero()) {
            throw new IllegalArgumentException("reservationWindow must be greater than zero");
        }
        this.reservations = new ArrayList<>();
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public Duration getReservationWindow() {
        return reservationWindow;
    }

    public Reservation reserve(Driver driver, Vehicle vehicle) {
        Validation.requireNonNull(driver, "driver");
        Validation.requireNonNull(vehicle, "vehicle");
        boolean ownedVehicle = driver.getVehicles().stream()
            .anyMatch(item -> item.getPlate().equalsIgnoreCase(vehicle.getPlate())
                && item.getType() == vehicle.getType());
        if (!ownedVehicle) {
            throw new IllegalArgumentException("The vehicle is not registered to this driver");
        }
        boolean activeReservationExists = reservations.stream()
            .anyMatch(reservation -> (reservation.getStatus() == ReservationStatus.RESERVED
                    || reservation.getStatus() == ReservationStatus.CHECKED_IN)
                && reservation.getDriverId().equals(driver.getId())
                && reservation.getPlate().equalsIgnoreCase(vehicle.getPlate()));
        if (activeReservationExists) {
            throw new IllegalStateException("This vehicle already has an active reservation");
        }

        Instant arrivalAt = Instant.now();
        Instant expiresAt;
        try {
            expiresAt = arrivalAt.plus(reservationWindow);
        } catch (ArithmeticException | java.time.DateTimeException exception) {
            throw new IllegalArgumentException("reservationWindow is too large", exception);
        }

        ParkingSpace space = parkingLot.assignSpace(vehicle);
        if (space == null) {
            throw new IllegalStateException("No compatible space is available");
        }

        Reservation reservation = new Reservation(
            UUID.randomUUID().toString(),
            driver.getId(),
            vehicle.getPlate(),
            vehicle.getType(),
            space,
            arrivalAt,
            expiresAt
        );
        reservations.add(reservation);
        return reservation;
    }

    public void cancel(Reservation reservation) {
        Reservation checkedReservation = Validation.requireNonNull(reservation, "reservation");
        if (!reservations.contains(checkedReservation)) {
            throw new IllegalArgumentException("Reservation is not managed by this service");
        }
        checkedReservation.cancel();
    }

    public void expireOverdue() {
        Instant now = Instant.now();
        for (Reservation reservation : reservations) {
            reservation.expireIfOverdue(now);
        }
    }
}