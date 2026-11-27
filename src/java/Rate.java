import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;

/**
 * Calculates parking fees from vehicle type and elapsed time.
 */
public class Rate {
    private final VehicleType vehicleType;
    private final BigDecimal hourlyAmount;

    public Rate(VehicleType vehicleType, BigDecimal hourlyAmount) {
        this.vehicleType = Objects.requireNonNull(vehicleType, "vehicleType");
        this.hourlyAmount = Objects.requireNonNull(hourlyAmount, "hourlyAmount");
        if (hourlyAmount.signum() < 0) {
            throw new IllegalArgumentException("hourlyAmount cannot be negative");
        }
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public BigDecimal getHourlyAmount() {
        return hourlyAmount;
    }

    public BigDecimal calculate(Duration duration) {
        Objects.requireNonNull(duration, "duration");
        long seconds = Math.max(0, duration.getSeconds());
        long hours = Math.max(1, (seconds + 3599) / 3600);
        return hourlyAmount.multiply(BigDecimal.valueOf(hours));
    }
}