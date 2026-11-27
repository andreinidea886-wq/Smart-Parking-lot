import java.math.BigDecimal;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class Rate {
    private final Map<VehicleType, BigDecimal> hourlyRates;
    public Rate(Map<VehicleType, BigDecimal> hourlyRates) {
        this.hourlyRates = new EnumMap<>(Objects.requireNonNull(hourlyRates, "hourlyRates"));
    }
    public BigDecimal getHourlyRate(VehicleType type) {
        BigDecimal rate = hourlyRates.get(type);
        if (rate == null) throw new IllegalArgumentException("No rate configured for " + type);
        return rate;
    }
    public BigDecimal calculate(Duration duration, VehicleType type) {
        Objects.requireNonNull(duration, "duration");
        long seconds = Math.max(0, duration.getSeconds());
        long hours = Math.max(1, (seconds + 3599) / 3600);
        return getHourlyRate(type).multiply(BigDecimal.valueOf(hours));
    }
}
