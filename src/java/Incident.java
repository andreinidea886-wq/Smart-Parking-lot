import java.time.Instant;
import java.util.Objects;

public class Incident {
    private final String id;
    private final String type;
    private final String details;
    private final ParkingSpace space;
    private final Instant createdAt;
    private String status;
    private String resolution;

    public Incident(String id, String type, String details, ParkingSpace space, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.details = Objects.requireNonNull(details, "details");
        this.space = space;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.status = "OPEN";
    }
    public String getId() { return id; }
    public String getType() { return type; }
    public String getDetails() { return details; }
    public ParkingSpace getSpace() { return space; }
    public Instant getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
    public String getResolution() { return resolution; }
    public void resolve(String note) {
        resolution = Objects.requireNonNull(note, "note");
        status = "RESOLVED";
    }
}
