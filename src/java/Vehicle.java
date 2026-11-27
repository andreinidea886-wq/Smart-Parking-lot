import java.util.Objects;

public class Vehicle {
    private final String plate;
    private final VehicleType type;

    public Vehicle(String plate, VehicleType type) {
        this.plate = Objects.requireNonNull(plate, "plate");
        this.type = Objects.requireNonNull(type, "type");
    }

    public String getPlate() { return plate; }
    public VehicleType getType() { return type; }
    public boolean isCompatible(ParkingSpace space) {
        return space != null && CompatibilityPolicy.isCompatible(type, space.getType());
    }
}
