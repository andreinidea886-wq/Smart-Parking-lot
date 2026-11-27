import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Stores parking spaces and finds spaces available for a vehicle.
 */
public class ParkingLot {
    private final String name;
    private final List<ParkingSpace> spaces;

    public ParkingLot(String name, List<ParkingSpace> spaces) {
        this.name = Objects.requireNonNull(name, "name");
        this.spaces = new ArrayList<>(Objects.requireNonNull(spaces, "spaces"));
    }

    public String getName() { return name; }
    public List<ParkingSpace> getSpaces() { return Collections.unmodifiableList(spaces); }
    public List<ParkingSpace> availableSpaces(VehicleType type) {
        return spaces.stream().filter(s -> s.getStatus() == SpaceStatus.AVAILABLE)
            .filter(s -> CompatibilityPolicy.isCompatible(type, s.getType())).collect(Collectors.toList());
    }
    public ParkingSpace assignSpace(Vehicle vehicle) {
        if (vehicle == null) return null;
        for (ParkingSpace space : availableSpaces(vehicle.getType())) {
            if (space.reserve()) return space;
        }
        return null;
    }
    public int getCapacity() { return spaces.size(); }
    public long getOccupiedCount() {
        return spaces.stream().filter(s -> s.getStatus() == SpaceStatus.OCCUPIED).count();
    }
}
