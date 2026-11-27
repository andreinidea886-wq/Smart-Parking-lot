import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Represents an attendant who records parking entries and exits.
 */
public class Attendant extends User {
    private final List<ParkingSession> assignedSessions;
    private final ParkingService parkingService;

    public Attendant(String id, String name, String contact, ParkingService parkingService) {
        super(id, name, contact);
        this.parkingService = Validation.requireNonNull(parkingService, "parkingService");
        this.assignedSessions = new ArrayList<>();
    }

    public List<ParkingSession> getAssignedSessions() {
        return Collections.unmodifiableList(assignedSessions);
    }

    public ParkingService getParkingService() {
        return parkingService;
    }

    public ParkingSession recordEntry(Vehicle vehicle) {
        Vehicle checkedVehicle = Validation.requireNonNull(vehicle, "vehicle");
        boolean alreadyParked = assignedSessions.stream()
            .anyMatch(session -> session.getStatus() == SessionStatus.ACTIVE
                && session.getVehicle().getPlate().equalsIgnoreCase(checkedVehicle.getPlate()));
        if (alreadyParked) {
            throw new IllegalStateException("This vehicle already has an active session with the attendant");
        }
        ParkingSession session = parkingService.startSession(checkedVehicle);
        assignedSessions.add(session);
        return session;
    }

    public void recordExit(ParkingSession session) {
        Validation.requireNonNull(session, "session");
        if (!assignedSessions.contains(session)) {
            throw new IllegalArgumentException("This session is not assigned to the attendant");
        }
        parkingService.endSession(session);
        assignedSessions.remove(session);
    }

    public void assistDriver(Driver driver) {
        Validation.requireNonNull(driver, "driver");
    }
}