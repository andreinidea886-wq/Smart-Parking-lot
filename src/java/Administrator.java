import java.util.Objects;

public class Administrator extends User {
    private final String role;

    public Administrator(String id, String name, String contact, String role) {
        super(id, name, contact);
        this.role = Objects.requireNonNull(role, "role");
    }
    public String getRole() { return role; }
    public void resolveIncident(Incident incident, String note) {
        Objects.requireNonNull(incident, "incident").resolve(note);
    }
    public void updateSpace(ParkingSpace space, SpaceStatus status) {
        Objects.requireNonNull(space, "space").setStatus(status);
    }
}
