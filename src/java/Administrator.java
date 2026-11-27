

/**
 * Represents staff who review incidents and manage parking spaces.
 */
public class Administrator extends User {
    private final String role;

    public Administrator(String id, String name, String contact, String role) {
        super(id, name, contact);
        this.role = Validation.requireText(role, "role");
    }
    public String getRole() { return role; }
    public void resolveIncident(Incident incident, String note) {
        Validation.requireNonNull(incident, "incident").resolve(note);
    }
    public void updateSpace(ParkingSpace space, SpaceStatus status) {
        Validation.requireNonNull(space, "space").setStatus(Validation.requireNonNull(status, "status"));
    }
}
