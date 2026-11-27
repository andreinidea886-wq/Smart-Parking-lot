import java.time.Duration;
import java.time.Instant;

/**
 * Records a vehicle parking stay from start to finish.
 */
public class ParkingSession {
    private final Vehicle vehicle;
    private final ParkingSpace space;
    private final Instant startedAt;
    private final String reservationReference;
    private Instant endedAt;
    private SessionStatus status;
    private boolean reminderSent;

    public ParkingSession(Vehicle vehicle, ParkingSpace space, Instant startedAt, String reservationReference) {
        this.vehicle = Validation.requireNonNull(vehicle, "vehicle");
        this.space = Validation.requireNonNull(space, "space");
        this.startedAt = Validation.requireNotFuture(startedAt, "startedAt");
        if (space.getStatus() != SpaceStatus.OCCUPIED) {
            throw new IllegalArgumentException("A session can only start in an occupied space");
        }
        this.reservationReference = reservationReference == null
            ? null : Validation.requireText(reservationReference, "reservationReference");
        this.status = SessionStatus.ACTIVE;
    }

    public Vehicle getVehicle() { return vehicle; }
    public ParkingSpace getSpace() { return space; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public String getReservationReference() { return reservationReference; }
    public SessionStatus getStatus() { return status; }
    public boolean isReminderSent() { return reminderSent; }

    public Duration duration() {
        Instant end = endedAt == null ? Instant.now() : endedAt;
        if (end.isBefore(startedAt)) {
            throw new IllegalStateException("Session end time cannot be before its start time");
        }
        return Duration.between(startedAt, end);
    }

    public void markReminderSent() {
        if (status != SessionStatus.ACTIVE) {
            throw new IllegalStateException("A reminder can only be sent for an active session");
        }
        if (reminderSent) {
            throw new IllegalStateException("A reminder was already sent for this session");
        }
        reminderSent = true;
    }

    public boolean close(Instant endTime) {
        Instant checkedEnd = Validation.requireNotFuture(endTime, "endTime");
        if (status != SessionStatus.ACTIVE) {
            throw new IllegalStateException("Only an active session can be closed");
        }
        if (checkedEnd.isBefore(startedAt)) {
            throw new IllegalArgumentException("endTime cannot be before startedAt");
        }
        space.release();
        endedAt = checkedEnd;
        status = SessionStatus.COMPLETED;
        return true;
    }
}