import java.time.Instant;
import java.util.Objects;

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
        this.reference = Objects.requireNonNull(reference, "reference");
        this.driverId = Objects.requireNonNull(driverId, "driverId");
        this.plate = Objects.requireNonNull(plate, "plate");
        this.vehicleType = Objects.requireNonNull(vehicleType, "vehicleType");
        this.space = Objects.requireNonNull(space, "space");
        this.arrivalAt = Objects.requireNonNull(arrivalAt, "arrivalAt");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
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
        if (status != ReservationStatus.RESERVED || !space.occupy()) return false;
        status = ReservationStatus.CHECKED_IN;
        return true;
    }
    public void cancel() {
        if (status == ReservationStatus.RESERVED) {
            status = ReservationStatus.CANCELLED;
            space.release();
        }
    }
    public void expire() {
        if (status == ReservationStatus.RESERVED) {
            status = ReservationStatus.EXPIRED;
            space.release();
        }
    }
    public void complete() {
        if (status == ReservationStatus.CHECKED_IN) status = ReservationStatus.COMPLETED;
    }
}
