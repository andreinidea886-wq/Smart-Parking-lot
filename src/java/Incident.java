import java.time.Instant;

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
        this.id = Validation.requireText(id, "id");
        this.type = Validation.requireText(type, "type");
        this.details = Validation.requireText(details, "details");
        this.space = space;
        this.createdAt = Validation.requireNotFuture(createdAt, "createdAt");
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
        String checkedNote = Validation.requireText(note, "resolution note");
        if (status != IncidentStatus.OPEN) {
            throw new IllegalStateException("Only an open incident can be resolved");
        }
        resolution = checkedNote;
        status = IncidentStatus.RESOLVED;
    }
}
