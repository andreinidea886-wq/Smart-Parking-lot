import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Coordinates space assignment and parking-session start and end.
 */
public class ParkingService {
    private final int maxSessionHours;
    private final ParkingLot parkingLot;

    public ParkingService(ParkingLot parkingLot, int maxSessionHours) {
        this.parkingLot = Objects.requireNonNull(parkingLot, "parkingLot");
        if (maxSessionHours <= 0) {
            throw new IllegalArgumentException("maxSessionHours must be positive");
        }
        this.maxSessionHours = maxSessionHours;
    }

    public int getMaxSessionHours() {
        return maxSessionHours;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public List<ParkingSpace> findAvailable(VehicleType type) {
        return parkingLot.availableSpaces(Objects.requireNonNull(type, "type"));
    }

    public ParkingSession startSession(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "vehicle");
        List<ParkingSpace> spaces = findAvailable(vehicle.getType());
        for (ParkingSpace space : spaces) {
            ParkingSession session = startSession(vehicle, space);
            if (session != null) {
                return session;
            }
        }
        throw new IllegalStateException("No compatible space is available");
    }

    public ParkingSession startSession(Vehicle vehicle, ParkingSpace space) {
        Objects.requireNonNull(vehicle, "vehicle");
        Objects.requireNonNull(space, "space");
        if (!vehicle.isCompatible(space) || !space.occupy()) {
            return null;
        }
        return new ParkingSession(vehicle, space, Instant.now(), null);
    }

    public void endSession(ParkingSession session) {
        Objects.requireNonNull(session, "session");
        if (!session.close(Instant.now())) {
            throw new IllegalStateException("The parking session is already closed");
        }
    }
}