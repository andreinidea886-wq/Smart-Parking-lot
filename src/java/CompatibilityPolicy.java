/**
 * Defines which vehicle types can use each parking-space type.
 */
public final class CompatibilityPolicy {
    private CompatibilityPolicy() { }

    public static boolean isCompatible(VehicleType vehicle, SpaceType space) {
        Validation.requireNonNull(vehicle, "vehicle");
        Validation.requireNonNull(space, "space");
        switch (vehicle) {
            case STANDARD: return space == SpaceType.STANDARD || space == SpaceType.ACCESSIBLE;
            case MOTORCYCLE: return space == SpaceType.MOTORCYCLE || space == SpaceType.STANDARD;
            case EV: return space == SpaceType.EV;
            case ACCESSIBLE: return space == SpaceType.ACCESSIBLE;
            default: return false;
        }
    }
}
