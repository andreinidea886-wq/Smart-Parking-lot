/**
 * Stores a vehicle plate and vehicle type.
 */
public class Vehicle {
    private final String plate;
    private final VehicleType type;

    public Vehicle(String plate, VehicleType type) {
        this.plate = Validation.requireText(plate, "plate");
        this.type = Validation.requireNonNull(type, "type");
    }

    public String getPlate() { return plate; }
    public VehicleType getType() { return type; }
    public boolean isCompatible(ParkingSpace space) {
        return space != null && CompatibilityPolicy.isCompatible(type, space.getType());
    }
}
