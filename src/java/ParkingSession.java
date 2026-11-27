import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

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
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle");
        this.space = Objects.requireNonNull(space, "space");
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
        this.reservationReference = reservationReference;
        this.status = SessionStatus.ACTIVE;
    }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSpace getSpace() { return space; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public String getReservationReference() { return reservationReference; }
    public SessionStatus getStatus() { return status; }
    public boolean isReminderSent() { return reminderSent; }
    public Duration duration() { return Duration.between(startedAt, endedAt == null ? Instant.now() : endedAt); }
    public void markReminderSent() { reminderSent = true; }
    public boolean close(Instant endTime) {
        if (status != SessionStatus.ACTIVE) return false;
        endedAt = Objects.requireNonNull(endTime, "endTime");
        status = SessionStatus.COMPLETED;
        space.release();
        return true;
    }
}
