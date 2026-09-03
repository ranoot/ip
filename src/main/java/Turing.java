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

    /** Indent placed before a task when a reply shows it on its own line. */
    private static final String TASK_INDENT = "  ";

    // The separators below are regular expressions rather than plain text, so that
    // "(?i)" can make them case insensitive and "\\s*" can absorb any spaces around
    // them. That way "/BY Sunday" and "book/by Sunday" are understood too.

    /** Pattern matching the "/by" separator of a deadline command. */
    private static final String BY_SEPARATOR_PATTERN = "(?i)\\s*/by\\s*";

    /** Pattern matching the "/from" separator of an event command. */
    private static final String FROM_SEPARATOR_PATTERN = "(?i)\\s*/from\\s*";

    /** Pattern matching the "/to" separator of an event command. */
    private static final String TO_SEPARATOR_PATTERN = "(?i)\\s*/to\\s*";

    /** Tasks entered so far. */
    private final TaskList tasks = new TaskList();

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
        return text.trim().replaceAll("\\s+", " ");
    }

    /**
     * Stores a task and confirms it to the user, refusing it if the list is full.
     *
     * @param task Task to store.
     */
    private void addTask(Task task) {
        if (tasks.isFull()) {
            reply("Sorry, I can only remember " + TaskList.MAX_TASKS + " tasks.");
            return;
        }

        tasks.add(task);
        reply("Got it. I've added this task:",
                TASK_INDENT + task,
                "Now you have " + tasks.getTaskCount() + " tasks in the list.");
    }

    /**
     * Adds a todo described by the text after the "todo" command word.
     *
     * @param description What the user has to do.
     */
    private void addTodo(String description) {
        if (description.isEmpty()) {
            reply("Please tell me what the todo is, e.g. todo borrow book");
            return;
        }

        addTask(new Todo(description));
    }

    /**
     * Splits text into the part before the separator and the part after it.
     * Both parts have to carry text for the split to count as successful, so
     * "return book /by" and "/by Sunday" are both rejected.
     *
     * @param text Text to split, such as "return book /by Sunday".
     * @param separatorPattern Regular expression matching the separator.
     * @return The two parts, or null if the separator is missing or a part is blank.
     */
    private static String[] splitAtSeparator(String text, String separatorPattern) {
        // Limit of 2 keeps any later occurrence of the separator inside the second part,
        // so "/by the 2nd /by lunchtime" is a due date rather than another split point.
        String[] parts = text.split(separatorPattern, 2);
        boolean hasBothParts = parts.length == 2 && !parts[0].isBlank() && !parts[1].isBlank();
        return hasBothParts ? parts : null;
    }

    /**
     * Adds a deadline described by the text after the "deadline" command word,
     * which is expected to read "<task> /by <when>".
     *
     * @param argument Text after the command word.
     */
    private void addDeadline(String argument) {
        String[] descriptionAndBy = splitAtSeparator(argument, BY_SEPARATOR_PATTERN);
        if (descriptionAndBy == null) {
            reply("Please use: deadline <task> /by <when>",
                    "e.g. deadline return book /by Sunday");
            return;
        }

        addTask(new Deadline(descriptionAndBy[0], descriptionAndBy[1]));
    }

    /**
     * Adds an event described by the text after the "event" command word, which
     * is expected to read "<task> /from <start> /to <end>".
     *
     * @param argument Text after the command word.
     */
    private void addEvent(String argument) {
        String[] descriptionAndTimes = splitAtSeparator(argument, FROM_SEPARATOR_PATTERN);
        if (descriptionAndTimes == null) {
            showEventUsage();
            return;
        }

        String[] startAndEnd = splitAtSeparator(descriptionAndTimes[1], TO_SEPARATOR_PATTERN);
        if (startAndEnd == null) {
            showEventUsage();
            return;
        }

        addTask(new Event(descriptionAndTimes[0], startAndEnd[0], startAndEnd[1]));
    }

    /** Reminds the user of the shape an event command has to take. */
    private static void showEventUsage() {
        reply("Please use: event <task> /from <start> /to <end>",
                "e.g. event project meeting /from Mon 2pm /to 4pm");
    }

    /**
     * Changes the done status of the task named by a "mark"/"unmark" command
     * and reports the outcome to the user.
     *
     * @param argument Text after the command word, expected to be a task number.
     * @param isDone True to mark the task as done, false to mark it as not done.
     */
    private void setDoneStatus(String argument, boolean isDone) {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException exception) {
            // The user typed something like "mark two", or nothing at all after "mark".
            reply("Please tell me the task number, e.g. mark 2");
            return;
        }

        if (!tasks.hasTaskNumber(taskNumber)) {
            reply("There is no task " + taskNumber + " in your list.");
            return;
        }

        Task task = tasks.getTask(taskNumber);
        String confirmation;
        if (isDone) {
            task.markAsDone();
            confirmation = "Nice! I've marked this task as done:";
        } else {
            task.markAsNotDone();
            confirmation = "OK, I've marked this task as not done yet:";
        }
        reply(confirmation, TASK_INDENT + task);
    }

    /**
     * Returns the lines listing every stored task, ready to be passed to reply.
     *
     * @return One header line followed by one line per task.
     */
    private String[] formatTaskList() {
        if (tasks.isEmpty()) {
            return new String[] {"There is nothing in your list yet."};
        }

        int taskCount = tasks.getTaskCount();
        String[] lines = new String[taskCount + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int taskNumber = 1; taskNumber <= taskCount; taskNumber++) {
            lines[taskNumber] = taskNumber + "." + tasks.getTask(taskNumber);
        }
        return lines;
    }

    /**
     * Carries out one line of user input and reports whether the chatbot should stop.
     *
     * @param input One line of input, with its whitespace already normalized.
     * @return True if the user asked to exit.
     */
    private boolean handleInput(String input) {
        // A blank line is almost certainly a stray Enter, so ask again
        // instead of treating it as a command.
        if (input.isEmpty()) {
            reply("Please type something so I know what to do.");
            return false;
        }

        // Split off the first word: it names the command, and the rest is its argument.
        // The limit of 2 keeps any remaining spaces inside the argument itself.
        String[] words = input.split(" ", 2);
        String keyword = words[0];
        String argument = words.length > 1 ? words[1] : "";

        switch (Command.fromKeyword(keyword)) {
        case BYE -> {
            reply("Bye. Hope to see you again soon!");
            return true;
        }
        case LIST -> reply(formatTaskList());
        case TODO -> addTodo(argument);
        case DEADLINE -> addDeadline(argument);
        case EVENT -> addEvent(argument);
        case MARK -> setDoneStatus(argument, true);
        case UNMARK -> setDoneStatus(argument, false);
        default -> reply("Sorry, I don't know what \"" + keyword + "\" means.",
                "Try one of: " + Command.getKeywords() + ".");
        }
        return false;
    }

    /**
     * Runs the chatbot, reading commands from standard input until the user
     * says goodbye or the input ends.
     */
    private void run() {
        showWelcome();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            boolean shouldExit = handleInput(normalizeWhitespace(scanner.nextLine()));
            if (shouldExit) {
                break;
            }
        }
    }

    /**
     * Starts one chatbot session.
     *
     * @param args Command line arguments, which are not used.
     */
    public static void main(String[] args) {
        new Turing().run();
    }
}
