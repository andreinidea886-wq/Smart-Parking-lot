import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/** Runs one complete parking and payment flow in the console. */
public final class ConsoleMain {
    private ConsoleMain() { }

    public static void main(String[] args) {
        try (Scanner input = new Scanner(System.in)) {
            System.out.println("=== Smart Parking Lot ===");
            System.out.println("1. Park a vehicle and pay");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            if (input.nextLine().trim().equals("1")) {
                runParkingFlow(input);
            } else {
                System.out.println("Goodbye.");
            }
        }
    }

    private static void runParkingFlow(Scanner input) {
        ParkingLot lot = createDemoParkingLot();
        ParkingService parkingService = new ParkingService(lot, 24);
        PaymentService paymentService = new PaymentService("Console demo", createDemoRates());

        System.out.print("Enter vehicle plate: ");
        Vehicle vehicle = new Vehicle(input.nextLine(), chooseVehicleType(input));

        ParkingSession session = parkingService.startSession(vehicle);
        System.out.println("Vehicle parked in space " + session.getSpace().getId() + ".");

        parkingService.endSession(session);
        System.out.println("Parking session completed.");

        Payment payment = paymentService.process(session, choosePaymentMethod(input));
        System.out.println("Payment complete: " + paymentService.issueReceipt(payment));
    }

    private static ParkingLot createDemoParkingLot() {
        return new ParkingLot("Console Demo Lot", List.of(
            new ParkingSpace("S-01", "Level 1", SpaceType.STANDARD),
            new ParkingSpace("M-01", "Level 1", SpaceType.MOTORCYCLE),
            new ParkingSpace("E-01", "Level 1", SpaceType.EV),
            new ParkingSpace("A-01", "Level 1", SpaceType.ACCESSIBLE)
        ));
    }

    private static Map<VehicleType, Rate> createDemoRates() {
        Map<VehicleType, Rate> rates = new EnumMap<>(VehicleType.class);
        for (VehicleType type : VehicleType.values()) {
            rates.put(type, new Rate(type, BigDecimal.valueOf(50)));
        }
        return rates;
    }

    private static VehicleType chooseVehicleType(Scanner input) {
        VehicleType[] types = VehicleType.values();
        System.out.println("Choose vehicle type:");
        for (int index = 0; index < types.length; index++) {
            System.out.println((index + 1) + ". " + types[index]);
        }
        return types[readChoice(input, types.length) - 1];
    }

    private static PaymentMethod choosePaymentMethod(Scanner input) {
        PaymentMethod[] methods = PaymentMethod.values();
        System.out.println("Choose payment method:");
        for (int index = 0; index < methods.length; index++) {
            System.out.println((index + 1) + ". " + methods[index]);
        }
        return methods[readChoice(input, methods.length) - 1];
    }

    private static int readChoice(Scanner input, int numberOfOptions) {
        while (true) {
            System.out.print("Enter a number (1-" + numberOfOptions + "): ");
            try {
                int choice = Integer.parseInt(input.nextLine().trim());
                if (choice >= 1 && choice <= numberOfOptions) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {
                // Ask again when the console input is not a menu number.
            }
            System.out.println("Please enter one of the listed numbers.");
        }
    }
}
