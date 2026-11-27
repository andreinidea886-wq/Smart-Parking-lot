import java.time.Instant;
import java.util.Objects;

/**
 * Records a parking issue and its resolution.
 */
public class Incident {
    private final String id;
    private final String type;
    private final String details;
    private final ParkingSpace space;
    private final Instant createdAt;
    private IncidentStatus status;
    private String resolution;

    public Incident(String id, String type, String details, ParkingSpace space, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.details = Objects.requireNonNull(details, "details");
        this.space = space;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.status = IncidentStatus.OPEN;
    }
    public String getId() { return id; }
    public String getType() { return type; }
    public String getDetails() { return details; }
    public ParkingSpace getSpace() { return space; }
    public Instant getCreatedAt() { return createdAt; }
    public IncidentStatus getStatus() { return status; }
    public String getResolution() { return resolution; }
    public void resolve(String note) {
        // Record the resolution and update the incident status together.
        resolution = Objects.requireNonNull(note, "note");
        status = IncidentStatus.RESOLVED;
    }
}
