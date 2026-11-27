import java.math.BigDecimal;
import java.time.Instant;

/**
 * Records a payment attempt and provides receipt text.
 */
public class Payment {
    private final String reference;
    private final BigDecimal amount;
    private final PaymentMethod method;
    private PaymentStatus status;
    private Instant paidAt;

    public Payment(String reference, BigDecimal amount, PaymentMethod method) {
        this.reference = Validation.requireText(reference, "reference");
        this.amount = Validation.requireNonNull(amount, "amount");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
        this.method = Validation.requireNonNull(method, "method");
        this.status = PaymentStatus.PENDING;
    }

    public String getReference() { return reference; }
    public BigDecimal getAmount() { return amount; }
    public PaymentMethod getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }
    public Instant getPaidAt() { return paidAt; }

    public void recordResult(boolean successful, Instant time) {
        Instant checkedTime = Validation.requireNotFuture(time, "time");
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("A payment result can only be recorded once");
        }
        status = successful ? PaymentStatus.PAID : PaymentStatus.FAILED;
        paidAt = successful ? checkedTime : null;
    }

    public String receipt() {
        return "Payment " + reference + " | PHP " + amount + " | " + method + " | " + status;
    }
}