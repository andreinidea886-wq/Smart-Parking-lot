import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Calculates and records payments for completed parking sessions.
 */
public class PaymentService {
    private final String provider;
    private final Map<VehicleType, Rate> rates;
    private final List<Payment> payments;

    public PaymentService(String provider, Map<VehicleType, Rate> rates) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.rates = new EnumMap<>(Objects.requireNonNull(rates, "rates"));
        this.payments = new ArrayList<>();
    }

    public String getProvider() {
        return provider;
    }

    public Map<VehicleType, Rate> getRates() {
        return Collections.unmodifiableMap(rates);
    }

    public List<Payment> getPayments() {
        return Collections.unmodifiableList(payments);
    }

    public Payment process(ParkingSession session, PaymentMethod method) {
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(method, "method");
        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new IllegalStateException("Close the parking session before payment");
        }
        VehicleType vehicleType = session.getVehicle().getType();
        Rate rate = rates.get(vehicleType);
        if (rate == null) {
            throw new IllegalStateException("No rate is configured for " + vehicleType);
        }
        BigDecimal amount = rate.calculate(Duration.between(session.getStartedAt(), session.getEndedAt()));
        Payment payment = new Payment(UUID.randomUUID().toString(), amount, method);
        payment.recordResult(true, Instant.now());
        payments.add(payment);
        return payment;
    }

    public String issueReceipt(Payment payment) {
        Objects.requireNonNull(payment, "payment");
        return payment.receipt();
    }
}