import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an attendant who records parking entries and exits.
 */
public class Attendant extends User {
    private final List<ParkingSession> assignedSessions;
    private final ParkingService parkingService;

    public Attendant(String id, String name, String contact, ParkingService parkingService) {
        super(id, name, contact);
        this.parkingService = Objects.requireNonNull(parkingService, "parkingService");
        this.assignedSessions = new ArrayList<>();
    }

    public List<ParkingSession> getAssignedSessions() {
        return Collections.unmodifiableList(assignedSessions);
    }

    public ParkingService getParkingService() {
        return parkingService;
    }

    public ParkingSession recordEntry(Vehicle vehicle) {
        ParkingSession session = parkingService.startSession(Objects.requireNonNull(vehicle, "vehicle"));
        assignedSessions.add(session);
        return session;
    }

    public void recordExit(ParkingSession session) {
        Objects.requireNonNull(session, "session");
        parkingService.endSession(session);
        assignedSessions.remove(session);
    }

    public void assistDriver(Driver driver) {
        Objects.requireNonNull(driver, "driver");
    }
}