import java.util.Scanner;

/**
 * Entry point of the Turing chatbot.
 * Supports greeting the user, adding todos, deadlines and events, listing the
 * stored tasks, marking them as done or not done, and exiting when the user
 * types "bye".
 */
public class Turing {
    /** Banner shown once when the chatbot starts. */
    private static final String BANNER = """
             _____ _   _ ____  ___ _   _  ____
            |_   _| | | |  _ \\|_ _| \\ | |/ ___|
              | | | | | | |_) || ||  \\| | |  _
              | | | |_| |  _ < | || |\\  | |_| |
              |_|  \\___/|_| \\_\\___|_| \\_|\\____|
            """;

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

    /** Command prefix that adds a task with no date/time, e.g. "todo borrow book". */
    private static final String TODO_COMMAND = "todo";

    /** Command prefix that adds a task with a due date, e.g. "deadline return book /by Sunday". */
    private static final String DEADLINE_COMMAND = "deadline";

    /** Command prefix that adds a task with a start and end, e.g. "event meeting /from 2pm /to 4pm". */
    private static final String EVENT_COMMAND = "event";

    // The separators below are regular expressions rather than plain text, so that
    // "(?i)" can make them case insensitive and "\\s*" can absorb any spaces around
    // them. That way "/BY Sunday" and "book/by Sunday" are understood too.

    /** Pattern matching the "/by" separator of a deadline command. */
    private static final String BY_SEPARATOR_PATTERN = "(?i)\\s*/by\\s*";

    /** Pattern matching the "/from" separator of an event command. */
    private static final String FROM_SEPARATOR_PATTERN = "(?i)\\s*/from\\s*";

    /** Pattern matching the "/to" separator of an event command. */
    private static final String TO_SEPARATOR_PATTERN = "(?i)\\s*/to\\s*";

    /** Upper bound on the number of tasks, as allowed by the requirements. */
    private static final int MAX_TASKS = 100;

    // Fixed-size array is enough here: the requirements cap the task count at 100.
    // Keeping the list in fields rather than in main lets every command handler
    // reach it without passing it through each call. A later increment replaces
    // this pair with a dedicated TaskList class.

    /** Tasks stored so far, filled from index 0 upwards. */
    private static Task[] tasks = new Task[MAX_TASKS];

    /** Number of entries of tasks that are in use, which is also the next free index. */
    private static int taskCount = 0;

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

    /** Prints the banner and the greeting shown when the chatbot starts. */
    private static void showWelcome() {
        System.out.println(BANNER);
        reply("Hello! I'm Turing", "What can I do for you?");
    }

    /**
     * Returns the given text with surrounding whitespace removed and every run
     * of internal whitespace collapsed into a single space. Cleaning the input
     * up front lets the rest of the chatbot treat "  mark    2  " exactly like
     * "mark 2", so no other code has to worry about stray spaces or tabs.
     *
     * @param text Raw line typed by the user.
     * @return Text with normalized whitespace.
     */
    private static String normalizeWhitespace(String text) {
        // "\\s+" matches one or more whitespace characters (spaces, tabs, ...).
        return text.trim().replaceAll("\\s+", " ");
    }

    /**
     * Stores a task and confirms it to the user, refusing it if the list is full.
     *
     * @param task Task to store.
     */
    private static void addTask(Task task) {
        if (taskCount == MAX_TASKS) {
            reply("Sorry, I can only remember " + MAX_TASKS + " tasks.");
            return;
        }

        tasks[taskCount] = task;
        taskCount++;
        reply("Got it. I've added this task:",
                "  " + task,
                "Now you have " + taskCount + " tasks in the list.");
    }

    /**
     * Adds a todo described by the text after the "todo" command word.
     *
     * @param description What the user has to do.
     */
    private static void addTodo(String description) {
        if (description.isEmpty()) {
            reply("Please tell me what the todo is, e.g. todo borrow book");
            return;
        }

        addTask(new Todo(description));
    }

    /**
     * Adds a deadline described by the text after the "deadline" command word,
     * which is expected to read "<task> /by <when>".
     *
     * @param argument Text after the command word.
     */
    private static void addDeadline(String argument) {
        // Limit of 2 keeps everything after the first "/by" together as the due date.
        String[] parts = argument.split(BY_SEPARATOR_PATTERN, 2);
        boolean hasBothParts = parts.length == 2 && !parts[0].isBlank() && !parts[1].isBlank();
        if (!hasBothParts) {
            reply("Please use: deadline <task> /by <when>",
                    "e.g. deadline return book /by Sunday");
            return;
        }

        addTask(new Deadline(parts[0], parts[1]));
    }

    /**
     * Adds an event described by the text after the "event" command word, which
     * is expected to read "<task> /from <start> /to <end>".
     *
     * @param argument Text after the command word.
     */
    private static void addEvent(String argument) {
        // Split at "/from" first, then split whatever follows it at "/to".
        String[] descriptionAndTimes = argument.split(FROM_SEPARATOR_PATTERN, 2);
        String[] times = descriptionAndTimes.length == 2
                ? descriptionAndTimes[1].split(TO_SEPARATOR_PATTERN, 2)
                : new String[0];

        boolean hasAllParts = times.length == 2
                && !descriptionAndTimes[0].isBlank()
                && !times[0].isBlank()
                && !times[1].isBlank();
        if (!hasAllParts) {
            reply("Please use: event <task> /from <start> /to <end>",
                    "e.g. event project meeting /from Mon 2pm /to 4pm");
            return;
        }

        addTask(new Event(descriptionAndTimes[0], times[0], times[1]));
    }

    /**
     * Changes the done status of the task named by a "mark"/"unmark" command
     * and reports the outcome to the user.
     *
     * @param argument Text after the command word, expected to be a task number.
     * @param isDone True to mark the task as done, false to mark it as not done.
     */
    private static void setDoneStatus(String argument, boolean isDone) {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException exception) {
            // The user typed something like "mark two", or nothing at all after "mark".
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
     * Returns the lines listing every stored task, ready to be passed to reply.
     *
     * @return One header line followed by one line per task.
     */
    private static String[] formatTaskList() {
        if (taskCount == 0) {
            return new String[] {"There is nothing in your list yet."};
        }

        String[] lines = new String[taskCount + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < taskCount; i++) {
            lines[i + 1] = (i + 1) + "." + tasks[i];
        }
        return lines;
    }

    /**
     * Carries out one line of user input and reports whether the chatbot should stop.
     *
     * @param input One line of input, with its whitespace already normalized.
     * @return True if the user asked to exit.
     */
    private static boolean handleInput(String input) {
        // A blank line is almost certainly a stray Enter, so ask again
        // instead of treating it as a command.
        if (input.isEmpty()) {
            reply("Please type something so I know what to do.");
            return false;
        }

        // Split off the first word: it is the command, and the rest is its argument.
        // The limit of 2 keeps any remaining spaces inside the argument itself.
        String[] words = input.split(" ", 2);
        String command = words[0];
        String argument = words.length > 1 ? words[1] : "";

        // Hand the argument to the matching command. equalsIgnoreCase accepts
        // "BYE", "Bye" and "bye" alike, so capitalization does not matter.
        if (command.equalsIgnoreCase(EXIT_COMMAND)) {
            reply("Bye. Hope to see you again soon!");
            return true;
        } else if (command.equalsIgnoreCase(LIST_COMMAND)) {
            reply(formatTaskList());
        } else if (command.equalsIgnoreCase(TODO_COMMAND)) {
            addTodo(argument);
        } else if (command.equalsIgnoreCase(DEADLINE_COMMAND)) {
            addDeadline(argument);
        } else if (command.equalsIgnoreCase(EVENT_COMMAND)) {
            addEvent(argument);
        } else if (command.equalsIgnoreCase(MARK_COMMAND)) {
            setDoneStatus(argument, true);
        } else if (command.equalsIgnoreCase(UNMARK_COMMAND)) {
            setDoneStatus(argument, false);
        } else {
            reply("Sorry, I don't know what \"" + command + "\" means.",
                    "Try: todo, deadline, event, list, mark, unmark or bye.");
        }
        return false;
    }

    /**
     * Runs the chatbot, reading commands from standard input until the user
     * says goodbye or the input ends.
     *
     * @param args Command line arguments, which are not used.
     */
    public static void main(String[] args) {
        showWelcome();

        // Scanner reads the user's input line by line from standard input.
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            boolean shouldExit = handleInput(normalizeWhitespace(scanner.nextLine()));
            if (shouldExit) {
                break;
            }
        }
    }
}
