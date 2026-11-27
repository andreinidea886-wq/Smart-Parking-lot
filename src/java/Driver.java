import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public void addVehicle(Vehicle vehicle) {
        Vehicle checkedVehicle = Validation.requireNonNull(vehicle, "vehicle");
        boolean duplicatePlate = vehicles.stream()
            .anyMatch(existing -> existing.getPlate().equalsIgnoreCase(checkedVehicle.getPlate()));
        if (duplicatePlate) {
            throw new IllegalArgumentException("A vehicle with plate " + checkedVehicle.getPlate() + " is already registered");
        }
        vehicles.add(checkedVehicle);
    }

    public boolean removeVehicle(Vehicle vehicle) {
        Vehicle checkedVehicle = Validation.requireNonNull(vehicle, "vehicle");
        if (!vehicles.remove(checkedVehicle)) {
            throw new IllegalArgumentException("The vehicle is not registered to this driver");
        }
        return true;
    }
}
