import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class Payment {
    private final String reference;
    private final BigDecimal amount;
    private final PaymentMethod method;
    private PaymentStatus status;
    private Instant paidAt;

    public Payment(String reference, BigDecimal amount, PaymentMethod method) {
        this.reference = Objects.requireNonNull(reference, "reference");
        this.amount = Objects.requireNonNull(amount, "amount");
        if (amount.signum() < 0) throw new IllegalArgumentException("amount cannot be negative");
        this.method = Objects.requireNonNull(method, "method");
        this.status = PaymentStatus.PENDING;
    }
    public String getReference() { return reference; }
    public BigDecimal getAmount() { return amount; }
    public PaymentMethod getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }
    public Instant getPaidAt() { return paidAt; }
    public void recordResult(boolean successful, Instant time) {
        status = successful ? PaymentStatus.PAID : PaymentStatus.FAILED;
        paidAt = successful ? Objects.requireNonNull(time, "time") : null;
    }
    public String receipt() {
        return "Payment " + reference + " | PHP " + amount + " | " + method + " | " + status;
    }
}
