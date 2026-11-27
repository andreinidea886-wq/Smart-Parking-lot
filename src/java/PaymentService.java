import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Calculates and records payments for completed parking sessions.
 */
public class PaymentService {
    private final String provider;
    private final Map<VehicleType, Rate> rates;
    private final List<Payment> payments;
    private final Set<ParkingSession> paidSessions;

    public PaymentService(String provider, Map<VehicleType, Rate> rates) {
        this.provider = Validation.requireText(provider, "provider");
        Map<VehicleType, Rate> checkedRates = Validation.requireNonNull(rates, "rates");
        if (checkedRates.isEmpty()) {
            throw new IllegalArgumentException("rates must contain at least one vehicle rate");
        }
        this.rates = new EnumMap<>(VehicleType.class);
        checkedRates.forEach((type, rate) -> {
            VehicleType checkedType = Validation.requireNonNull(type, "rate vehicle type");
            Rate checkedRate = Validation.requireNonNull(rate, "rate");
            if (checkedRate.getVehicleType() != checkedType) {
                throw new IllegalArgumentException("Rate vehicle type must match its map key");
            }
            this.rates.put(checkedType, checkedRate);
        });
        this.payments = new ArrayList<>();
        this.paidSessions = Collections.newSetFromMap(new IdentityHashMap<>());
    }

    public String getProvider() { return provider; }

    public Map<VehicleType, Rate> getRates() {
        return Collections.unmodifiableMap(rates);
    }

    public List<Payment> getPayments() {
        return Collections.unmodifiableList(payments);
    }

    public Payment process(ParkingSession session, PaymentMethod method) {
        ParkingSession checkedSession = Validation.requireNonNull(session, "session");
        PaymentMethod checkedMethod = Validation.requireNonNull(method, "method");
        if (checkedSession.getStatus() != SessionStatus.COMPLETED || checkedSession.getEndedAt() == null) {
            throw new IllegalStateException("Close the parking session before payment");
        }
        if (paidSessions.contains(checkedSession)) {
            throw new IllegalStateException("This parking session has already been paid");
        }
        VehicleType vehicleType = checkedSession.getVehicle().getType();
        Rate rate = rates.get(vehicleType);
        if (rate == null) {
            throw new IllegalStateException("No rate is configured for " + vehicleType);
        }

        Duration duration = Duration.between(checkedSession.getStartedAt(), checkedSession.getEndedAt());
        BigDecimal amount = rate.calculate(duration);
        Payment payment = new Payment(UUID.randomUUID().toString(), amount, checkedMethod);
        payment.recordResult(true, Instant.now());
        payments.add(payment);
        paidSessions.add(checkedSession);
        return payment;
    }

    public String issueReceipt(Payment payment) {
        Payment checkedPayment = Validation.requireNonNull(payment, "payment");
        if (checkedPayment.getStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("A receipt can only be issued for a paid payment");
        }
        return checkedPayment.receipt();
    }
}