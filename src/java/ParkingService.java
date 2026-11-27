import java.time.Instant;
import java.util.List;


/**
 * Coordinates space assignment and parking-session start and end.
 */
public class ParkingService {
    private final int maxSessionHours;
    private final ParkingLot parkingLot;

    public ParkingService(ParkingLot parkingLot, int maxSessionHours) {
        this.parkingLot = Validation.requireNonNull(parkingLot, "parkingLot");
        this.maxSessionHours = Validation.requirePositive(maxSessionHours, "maxSessionHours");
    }

    public int getMaxSessionHours() {
        return maxSessionHours;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public List<ParkingSpace> findAvailable(VehicleType type) {
        return parkingLot.availableSpaces(Validation.requireNonNull(type, "type"));
    }

    public ParkingSession startSession(Vehicle vehicle) {
        Validation.requireNonNull(vehicle, "vehicle");
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
        Validation.requireNonNull(vehicle, "vehicle");
        Validation.requireNonNull(space, "space");
        if (parkingLot.getSpaces().stream().noneMatch(candidate -> candidate == space)) {
            throw new IllegalArgumentException("The space does not belong to this parking lot");
        }
        if (!vehicle.isCompatible(space)) {
            throw new IllegalArgumentException("The space is not compatible with this vehicle");
        }
        if (space.getStatus() != SpaceStatus.AVAILABLE) {
            throw new IllegalStateException("A walk-in session requires an available space; reserved spaces must check in through a reservation");
        }
        space.occupy();
        return new ParkingSession(vehicle, space, Instant.now(), null);
    }

    public void endSession(ParkingSession session) {
        Validation.requireNonNull(session, "session");
        if (!session.close(Instant.now())) {
            throw new IllegalStateException("The parking session is already closed");
        }
    }
}