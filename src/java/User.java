

/**
 * Stores account details shared by the system user roles.
 */
public abstract class User {
    // Private fields keep callers from changing account details directly.
    private final String id;
    private final String name;
    private final String contact;

    protected User(String id, String name, String contact) {
        this.id = Validation.requireText(id, "id");
        this.name = Validation.requireText(name, "name");
        this.contact = Validation.requireText(contact, "contact");
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getContact() { return contact; }
}
