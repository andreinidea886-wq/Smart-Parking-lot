import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a driver and the vehicles registered to them.
 */
public class Driver extends User {
    private final List<Vehicle> vehicles;

    public Driver(String id, String name, String contact) {
        super(id, name, contact);
        this.vehicles = new ArrayList<>();
    }
    // Return a read-only view; vehicle changes go through the methods below.
    public List<Vehicle> getVehicles() { return Collections.unmodifiableList(vehicles); }
    public void addVehicle(Vehicle vehicle) { vehicles.add(Objects.requireNonNull(vehicle, "vehicle")); }
    public boolean removeVehicle(Vehicle vehicle) { return vehicles.remove(vehicle); }
}
