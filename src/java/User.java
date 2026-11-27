import java.util.Objects;

public abstract class User {
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
