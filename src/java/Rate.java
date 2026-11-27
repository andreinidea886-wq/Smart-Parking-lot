import java.math.BigDecimal;
import java.time.Duration;

/**
 * Calculates parking fees from vehicle type and elapsed time.
 */
public class Rate {
    private final VehicleType vehicleType;
    private final BigDecimal hourlyAmount;

    public Rate(VehicleType vehicleType, BigDecimal hourlyAmount) {
        this.vehicleType = Validation.requireNonNull(vehicleType, "vehicleType");
        this.hourlyAmount = Validation.requireNonNull(hourlyAmount, "hourlyAmount");
        if (hourlyAmount.signum() < 0) {
            throw new IllegalArgumentException("hourlyAmount cannot be negative");
        }
    }

    public VehicleType getVehicleType() { return vehicleType; }
    public BigDecimal getHourlyAmount() { return hourlyAmount; }

    public BigDecimal calculate(Duration duration) {
        Duration checkedDuration = Validation.requireNonNull(duration, "duration");
        if (checkedDuration.isNegative()) {
            throw new IllegalArgumentException("duration cannot be negative");
        }
        long seconds = checkedDuration.getSeconds();
        boolean partialHour = seconds % 3600 != 0 || checkedDuration.getNano() != 0;
        long hours = Math.max(1, seconds / 3600 + (partialHour ? 1 : 0));
        return hourlyAmount.multiply(BigDecimal.valueOf(hours));
    }
}