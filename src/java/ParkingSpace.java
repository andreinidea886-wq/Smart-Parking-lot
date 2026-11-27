import java.util.Objects;

/**
 * Represents one parking space and tracks its availability.
 */
public class ParkingSpace {
    private final String id;
    private final String level;
    private final SpaceType type;
    private SpaceStatus status;

    public ParkingSpace(String id, String level, SpaceType type) {
        this(id, level, type, SpaceStatus.AVAILABLE);
    }

    public ParkingSpace(String id, String level, SpaceType type, SpaceStatus status) {
        this.id = Objects.requireNonNull(id, "id");
        this.level = Objects.requireNonNull(level, "level");
        this.type = Objects.requireNonNull(type, "type");
        this.status = Objects.requireNonNull(status, "status");
    }

    public String getId() { return id; }
    public String getLevel() { return level; }
    public SpaceType getType() { return type; }
    public SpaceStatus getStatus() { return status; }
    public boolean reserve() {
        if (status != SpaceStatus.AVAILABLE) return false;
        status = SpaceStatus.RESERVED;
        return true;
    }
    public boolean occupy() {
        if (status != SpaceStatus.AVAILABLE && status != SpaceStatus.RESERVED) return false;
        status = SpaceStatus.OCCUPIED;
        return true;
    }
    public void release() {
        if (status == SpaceStatus.OCCUPIED || status == SpaceStatus.RESERVED) status = SpaceStatus.AVAILABLE;
    }
    public void setOutOfService() { status = SpaceStatus.OUT_OF_SERVICE; }
    public void setStatus(SpaceStatus status) { this.status = Objects.requireNonNull(status, "status"); }
}
