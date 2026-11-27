import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Coordinates reservation creation, cancellation, and expiry.
 */
public class ReservationService {
    private final List<Reservation> reservations;
    private final ParkingLot parkingLot;
    private final Duration reservationWindow;

    public ReservationService(ParkingLot parkingLot, Duration reservationWindow) {
        this.parkingLot = Objects.requireNonNull(parkingLot, "parkingLot");
        this.reservationWindow = Objects.requireNonNull(reservationWindow, "reservationWindow");
        if (reservationWindow.isNegative() || reservationWindow.isZero()) {
            throw new IllegalArgumentException("reservationWindow must be positive");
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
        Objects.requireNonNull(driver, "driver");
        Objects.requireNonNull(vehicle, "vehicle");
        boolean ownedVehicle = driver.getVehicles().stream()
            .anyMatch(item -> item.getPlate().equals(vehicle.getPlate()));
        if (!ownedVehicle) {
            throw new IllegalArgumentException("The vehicle is not registered to this driver");
        }

        ParkingSpace space = parkingLot.assignSpace(vehicle);
        if (space == null) {
            throw new IllegalStateException("No compatible space is available");
        }

        Instant arrivalAt = Instant.now();
        Reservation reservation = new Reservation(
            UUID.randomUUID().toString(),
            driver.getId(),
            vehicle.getPlate(),
            vehicle.getType(),
            space,
            arrivalAt,
            arrivalAt.plus(reservationWindow)
        );
        reservations.add(reservation);
        return reservation;
    }

    public void cancel(Reservation reservation) {
        if (reservations.contains(Objects.requireNonNull(reservation, "reservation"))) {
            reservation.cancel();
        }
    }

    public void expireOverdue() {
        Instant now = Instant.now();
        for (Reservation reservation : reservations) {
            reservation.expireIfOverdue(now);
        }
    }
}