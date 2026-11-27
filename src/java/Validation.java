import java.time.Instant;
import java.util.Objects;

/** Shared checks for user-provided values used by the model classes. */
public final class Validation {
    private Validation() {
    }

    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    public static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }

    public static Instant requireNotFuture(Instant value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.isAfter(Instant.now())) {
            throw new IllegalArgumentException(fieldName + " cannot be in the future");
        }
        return value;
    }

    public static int requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
        return value;
    }
}