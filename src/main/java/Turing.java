import java.util.Scanner;

/**
 * Entry point of the Turing chatbot.
 * Currently supports greeting the user, storing whatever text is typed,
 * listing the stored items on request, and exiting when the user types "bye".
 */
public class Turing {
    /** Horizontal divider printed around every chatbot response. */
    private static final String DIVIDER = "____________________________________________________________";

    /** Command that ends the conversation. */
    private static final String EXIT_COMMAND = "bye";

    /** Command that shows everything stored so far. */
    private static final String LIST_COMMAND = "list";

    /** Upper bound on the number of items, as allowed by the requirements. */
    private static final int MAX_TASKS = 100;

    /**
     * Prints one or more lines wrapped between two dividers, so that every
     * chatbot reply has a consistent look.
     *
     * @param lines the lines of text to show to the user
     */
    private static void reply(String... lines) {
        System.out.println(DIVIDER);
        for (String line : lines) {
            System.out.println(" " + line);
        }
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

        reply("Hello! I'm Turing", "What can I do for you?");

        // Fixed-size array is enough here: the requirements cap the item count at 100.
        // taskCount doubles as the number of stored items and the next free index.
        String[] tasks = new String[MAX_TASKS];
        int taskCount = 0;

        // Scanner reads the user's input line by line from standard input.
        Scanner in = new Scanner(System.in);

        // Keep handling commands until the user types "bye" (or input runs out).
        while (in.hasNextLine()) {
            String input = in.nextLine();

            if (input.equals(EXIT_COMMAND)) {
                reply("Bye. Hope to see you again soon!");
                break;
            } else if (input.equals(LIST_COMMAND)) {
                // Build the numbered list as one line per stored item.
                String[] lines = new String[taskCount];
                for (int i = 0; i < taskCount; i++) {
                    lines[i] = (i + 1) + ". " + tasks[i];
                }
                reply(lines);
            } else if (taskCount < MAX_TASKS) {
                tasks[taskCount] = input;
                taskCount++;
                reply("added: " + input);
            } else {
                reply("Sorry, I can only remember " + MAX_TASKS + " items.");
            }
        }
    }
}
