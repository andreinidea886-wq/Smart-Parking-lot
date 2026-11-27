import java.util.Objects;

/**
 * Stores account details shared by the system user roles.
 */
public abstract class User {
    // Private fields keep callers from changing account details directly.
    private final String id;
    private final String name;
    private final String contact;

    protected User(String id, String name, String contact) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.contact = Objects.requireNonNull(contact, "contact");
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getContact() { return contact; }
}
