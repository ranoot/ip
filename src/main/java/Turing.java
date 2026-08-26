import java.util.Scanner;

/**
 * Entry point of the Turing chatbot.
 * Currently supports greeting the user, echoing whatever is typed,
 * and exiting when the user types "bye".
 */
public class Turing {
    /** Horizontal divider printed around every chatbot response. */
    private static final String DIVIDER = "____________________________________________________________";

    /** Command that ends the conversation. */
    private static final String EXIT_COMMAND = "bye";

    /**
     * Prints a message wrapped between two dividers, so that every
     * chatbot reply has a consistent look.
     *
     * @param message the text to show to the user
     */
    private static void reply(String message) {
        System.out.println(DIVIDER);
        System.out.println(" " + message);
        System.out.println(DIVIDER);
        System.out.println();
    }

    public static void main(String[] args) {
        String banner = " _____ _   _ ____  ___ _   _  ____ \n"
                + "|_   _| | | |  _ \\|_ _| \\ | |/ ___|\n"
                + "  | | | | | | |_) || ||  \\| | |  _ \n"
                + "  | | | |_| |  _ < | || |\\  | |_| |\n"
                + "  |_|  \\___/|_| \\_\\___|_| \\_|\\____|\n";
        System.out.println(banner);

        System.out.println(DIVIDER);
        System.out.println(" Hello! I'm Turing");
        System.out.println(" What can I do for you?");
        System.out.println(DIVIDER);
        System.out.println();

        // Scanner reads the user's input line by line from standard input.
        Scanner in = new Scanner(System.in);

        // Keep echoing input until the user types "bye" (or input runs out).
        while (in.hasNextLine()) {
            String input = in.nextLine();
            if (input.equals(EXIT_COMMAND)) {
                reply("Bye. Hope to see you again soon!");
                break;
            }
            reply(input);
        }
    }
}
