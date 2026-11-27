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
        this.name = Validation.requireText(name, "name");
        List<ParkingSpace> checkedSpaces = new ArrayList<>(Validation.requireNonNull(spaces, "spaces"));
        if (checkedSpaces.isEmpty()) {
            throw new IllegalArgumentException("spaces must contain at least one parking space");
        }
        if (checkedSpaces.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("spaces must not contain null entries");
        }
        long uniqueIds = checkedSpaces.stream().map(ParkingSpace::getId).distinct().count();
        if (uniqueIds != checkedSpaces.size()) {
            throw new IllegalArgumentException("parking space ids must be unique");
        }
        this.spaces = checkedSpaces;
    }

    public String getName() { return name; }
    public List<ParkingSpace> getSpaces() { return Collections.unmodifiableList(spaces); }
    public List<ParkingSpace> availableSpaces(VehicleType type) {
        Validation.requireNonNull(type, "type");
        return spaces.stream().filter(s -> s.getStatus() == SpaceStatus.AVAILABLE)
            .filter(s -> CompatibilityPolicy.isCompatible(type, s.getType())).collect(Collectors.toList());
    }
    public ParkingSpace assignSpace(Vehicle vehicle) {
        Validation.requireNonNull(vehicle, "vehicle");
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
