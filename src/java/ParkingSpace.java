
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
        this.id = Validation.requireText(id, "id");
        this.level = Validation.requireText(level, "level");
        this.type = Validation.requireNonNull(type, "type");
        this.status = Validation.requireNonNull(status, "status");
    }

    public String getId() { return id; }
    public String getLevel() { return level; }
    public SpaceType getType() { return type; }
    public SpaceStatus getStatus() { return status; }

    public boolean reserve() {
        if (status != SpaceStatus.AVAILABLE) {
            throw new IllegalStateException("Only an available space can be reserved");
        }
        status = SpaceStatus.RESERVED;
        return true;
    }

    public boolean occupy() {
        if (status != SpaceStatus.AVAILABLE && status != SpaceStatus.RESERVED) {
            throw new IllegalStateException("Only an available or reserved space can be occupied");
        }
        status = SpaceStatus.OCCUPIED;
        return true;
    }

    public void release() {
        if (status != SpaceStatus.OCCUPIED && status != SpaceStatus.RESERVED) {
            throw new IllegalStateException("Only an occupied or reserved space can be released");
        }
        status = SpaceStatus.AVAILABLE;
    }

    public void setOutOfService() {
        setStatus(SpaceStatus.OUT_OF_SERVICE);
    }

    /** Allows an administrator to correct a status after validating the requested value. */
    public void setStatus(SpaceStatus requestedStatus) {
        SpaceStatus checkedStatus = Validation.requireNonNull(requestedStatus, "status");
        if (checkedStatus == SpaceStatus.OUT_OF_SERVICE
                && (status == SpaceStatus.OCCUPIED || status == SpaceStatus.RESERVED)) {
            throw new IllegalStateException("Release the space before marking it out of service");
        }
        if (status == SpaceStatus.OUT_OF_SERVICE
                && checkedStatus != SpaceStatus.OUT_OF_SERVICE
                && checkedStatus != SpaceStatus.AVAILABLE) {
            throw new IllegalStateException("An out-of-service space must be made available before use");
        }
        this.status = checkedStatus;
    }
}