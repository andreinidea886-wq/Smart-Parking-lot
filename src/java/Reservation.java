import java.time.Instant;

/**
 * Holds a parking space for a driver until arrival or expiry.
 */
public class Reservation {
    private final String reference;
    private final String driverId;
    private final String plate;
    private final VehicleType vehicleType;
    private final ParkingSpace space;
    private final Instant arrivalAt;
    private final Instant expiresAt;
    private ReservationStatus status;

    public Reservation(String reference, String driverId, String plate, VehicleType vehicleType,
                       ParkingSpace space, Instant arrivalAt, Instant expiresAt) {
        this.reference = Validation.requireText(reference, "reference");
        this.driverId = Validation.requireText(driverId, "driverId");
        this.plate = Validation.requireText(plate, "plate");
        this.vehicleType = Validation.requireNonNull(vehicleType, "vehicleType");
        this.space = Validation.requireNonNull(space, "space");
        this.arrivalAt = Validation.requireNotFuture(arrivalAt, "arrivalAt");
        this.expiresAt = Validation.requireNonNull(expiresAt, "expiresAt");
        if (!expiresAt.isAfter(arrivalAt)) {
            throw new IllegalArgumentException("expiresAt must be after arrivalAt");
        }
        if (!CompatibilityPolicy.isCompatible(vehicleType, space.getType())) {
            throw new IllegalArgumentException("The reserved space must be compatible with the vehicle type");
        }
        if (space.getStatus() != SpaceStatus.RESERVED) {
            throw new IllegalArgumentException("A reservation requires a space that is already reserved");
        }

        this.status = ReservationStatus.RESERVED;
    }

    public String getReference() { return reference; }
    public String getDriverId() { return driverId; }
    public String getPlate() { return plate; }
    public VehicleType getVehicleType() { return vehicleType; }
    public ParkingSpace getSpace() { return space; }
    public Instant getArrivalAt() { return arrivalAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public ReservationStatus getStatus() { return status; }

    public boolean checkIn() {
        requireStatus(ReservationStatus.RESERVED, "Only an active reservation can be checked in");
        if (!expiresAt.isAfter(Instant.now())) {
            throw new IllegalStateException("This reservation has expired");
        }
        space.occupy();
        status = ReservationStatus.CHECKED_IN;
        return true;
    }

    public void cancel() {
        requireStatus(ReservationStatus.RESERVED, "Only an active reservation can be cancelled");
        space.release();
        status = ReservationStatus.CANCELLED;
    }

    public void expire() {
        requireStatus(ReservationStatus.RESERVED, "Only an active reservation can expire");
        if (expiresAt.isAfter(Instant.now())) {
            throw new IllegalStateException("A reservation cannot expire before expiresAt");
        }
        space.release();
        status = ReservationStatus.EXPIRED;
    }

    public boolean expireIfOverdue(Instant now) {
        Instant checkedNow = Validation.requireNotFuture(now, "now");
        if (status == ReservationStatus.RESERVED && !expiresAt.isAfter(checkedNow)) {
            expire();
            return true;
        }
        return false;
    }

    public void complete() {
        requireStatus(ReservationStatus.CHECKED_IN, "Only a checked-in reservation can be completed");
        status = ReservationStatus.COMPLETED;
    }

    private void requireStatus(ReservationStatus required, String message) {
        if (status != required) {
            throw new IllegalStateException(message + "; current status is " + status);
        }
    }
}