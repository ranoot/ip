import java.util.Scanner;

/**
 * Entry point of the Turing chatbot.
 * Currently supports greeting the user, storing whatever text is typed,
 * listing the stored tasks, marking them as done or not done, and exiting
 * when the user types "bye".
 */
public class Turing {
    /** Horizontal divider printed around every chatbot response. */
    private static final String DIVIDER = "____________________________________________________________";

    /** Command that ends the conversation. */
    private static final String EXIT_COMMAND = "bye";

    /** Command that shows everything stored so far. */
    private static final String LIST_COMMAND = "list";

    /** Command prefix that marks a task as done, e.g. "mark 2". */
    private static final String MARK_COMMAND = "mark";

    /** Command prefix that marks a task as not done, e.g. "unmark 2". */
    private static final String UNMARK_COMMAND = "unmark";

    /** Upper bound on the number of items, as allowed by the requirements. */
    private static final int MAX_TASKS = 100;

    /**
     * Prints one or more lines wrapped between two dividers, so that every
     * chatbot reply has a consistent look.
     *
     * @param lines Lines of text to show to the user.
     */
    private static void reply(String... lines) {
        System.out.println(DIVIDER);
        for (String line : lines) {
            System.out.println(" " + line);
        }
        System.out.println(DIVIDER);
        System.out.println();
    }

    /**
     * Changes the done status of the task named by a "mark"/"unmark" command
     * and reports the outcome to the user.
     *
     * @param tasks Stored tasks.
     * @param taskCount Number of entries of tasks that are in use.
     * @param argument Text after the command word, expected to be a task number.
     * @param isDone True to mark the task as done, false to mark it as not done.
     */
    private static void setDoneStatus(Task[] tasks, int taskCount, String argument, boolean isDone) {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument.trim());
        } catch (NumberFormatException exception) {
            // The user typed something like "mark two" instead of "mark 2".
            reply("Please tell me the task number, e.g. mark 2");
            return;
        }

        // Task numbers shown to the user start at 1, but array indexes start at 0.
        if (taskNumber < 1 || taskNumber > taskCount) {
            reply("There is no task " + taskNumber + " in your list.");
            return;
        }

        Task task = tasks[taskNumber - 1];
        if (isDone) {
            task.markAsDone();
            reply("Nice! I've marked this task as done:", "  " + task);
        } else {
            task.markAsNotDone();
            reply("OK, I've marked this task as not done yet:", "  " + task);
        }
    }

    /**
     * Runs the chatbot, reading commands from standard input until the user
     * says goodbye or the input ends.
     *
     * @param args Command line arguments, which are not used.
     */
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
        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

        // Scanner reads the user's input line by line from standard input.
        Scanner scanner = new Scanner(System.in);

        // Keep handling commands until the user types "bye" (or input runs out).
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();

            if (input.equals(EXIT_COMMAND)) {
                reply("Bye. Hope to see you again soon!");
                break;
            } else if (input.equals(LIST_COMMAND)) {
                // One line per task, preceded by a header line.
                String[] lines = new String[taskCount + 1];
                lines[0] = "Here are the tasks in your list:";
                for (int i = 0; i < taskCount; i++) {
                    lines[i + 1] = (i + 1) + "." + tasks[i];
                }
                reply(lines);
            } else if (input.startsWith(MARK_COMMAND + " ")) {
                setDoneStatus(tasks, taskCount, input.substring(MARK_COMMAND.length()), true);
            } else if (input.startsWith(UNMARK_COMMAND + " ")) {
                setDoneStatus(tasks, taskCount, input.substring(UNMARK_COMMAND.length()), false);
            } else if (taskCount < MAX_TASKS) {
                tasks[taskCount] = new Task(input);
                taskCount++;
                reply("added: " + input);
            } else {
                reply("Sorry, I can only remember " + MAX_TASKS + " items.");
            }
        }
    }
}
