package turing;

import turing.task.Deadline;
import turing.task.Event;
import turing.task.Task;
import turing.task.TaskList;
import turing.task.Todo;

/**
 * Entry point of the Turing chatbot.
 * Supports greeting the user, adding todos, deadlines and events, listing the
 * stored tasks, marking them as done or not done, and exiting when the user
 * types "bye".
 */
public class Turing {
    // The separators below are regular expressions rather than plain text, so that
    // "(?i)" can make them case insensitive and "\\s*" can absorb any spaces around
    // them. That way "/BY Sunday" and "book/by Sunday" are understood too.

    /** Pattern matching the "/by" separator of a deadline command. */
    private static final String BY_SEPARATOR_PATTERN = "(?i)\\s*/by\\s*";

    /** Pattern matching the "/from" separator of an event command. */
    private static final String FROM_SEPARATOR_PATTERN = "(?i)\\s*/from\\s*";

    /** Pattern matching the "/to" separator of an event command. */
    private static final String TO_SEPARATOR_PATTERN = "(?i)\\s*/to\\s*";

    /** Reminder of the shape an event command has to take. */
    private static final String EVENT_USAGE =
            "Please use: event <task> /from <start> /to <end>, e.g. event meeting /from Mon 2pm /to 4pm";

    /** Where the tasks are kept between runs, relative to where the chatbot is started. */
    private static final String SAVE_FILE_PATH = "data/turing.txt";

    /** Talks to the user: reads their commands and shows them every reply. */
    private final Ui ui = new Ui();

    /** Tasks entered so far. */
    private final TaskList tasks = new TaskList();

    /** Reads and writes the save file holding those tasks. */
    private final Storage storage = new Storage(SAVE_FILE_PATH);

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
     * Stores a task and confirms it to the user.
     *
     * @param task Task to store.
     */
    private void addTask(Task task) {
        tasks.add(task);
        ui.showTaskAdded(task, tasks.getTaskCount());
    }

    /**
     * Adds a todo described by the text after the "todo" command word.
     *
     * @param description What the user has to do.
     * @throws TuringException If the description is missing.
     */
    private void addTodo(String description) throws TuringException {
        if (description.isEmpty()) {
            throw new TuringException("A todo needs a description, or I have nothing to remember.",
                    "Please use: todo <task>, e.g. todo borrow book");
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
     * which is expected to read {@code <task> /by <when>}.
     *
     * @param argument Text after the command word.
     * @throws TuringException If the task or the due date is missing.
     */
    private void addDeadline(String argument) throws TuringException {
        String[] descriptionAndBy = splitAtSeparator(argument, BY_SEPARATOR_PATTERN);
        if (descriptionAndBy == null) {
            throw new TuringException("A deadline needs a task and a due date, separated by /by.",
                    "Please use: deadline <task> /by <when>, e.g. deadline return book /by Sunday");
        }

        addTask(new Deadline(descriptionAndBy[0], descriptionAndBy[1]));
    }

    /**
     * Adds an event described by the text after the "event" command word, which
     * is expected to read {@code <task> /from <start> /to <end>}.
     *
     * @param argument Text after the command word.
     * @throws TuringException If the task, the start or the end is missing.
     */
    private void addEvent(String argument) throws TuringException {
        String[] descriptionAndTimes = splitAtSeparator(argument, FROM_SEPARATOR_PATTERN);
        if (descriptionAndTimes == null) {
            throw new TuringException("An event needs a task and a start time, separated by /from.", EVENT_USAGE);
        }

        String[] startAndEnd = splitAtSeparator(descriptionAndTimes[1], TO_SEPARATOR_PATTERN);
        if (startAndEnd == null) {
            throw new TuringException("An event needs an end time after its start time, separated by /to.",
                    EVENT_USAGE);
        }

        addTask(new Event(descriptionAndTimes[0], startAndEnd[0], startAndEnd[1]));
    }

    /**
     * Changes the done status of the task named by a "mark"/"unmark" command
     * and reports the outcome to the user.
     *
     * @param argument Text after the command word, expected to be a task number.
     * @param isDone True to mark the task as done, false to mark it as not done.
     * @throws TuringException If the argument does not name a stored task.
     */
    private void setDoneStatus(String argument, boolean isDone) throws TuringException {
        // Naming the word the user actually typed keeps the advice in any error
        // message something they can copy straight back into the next command.
        String commandWord = isDone ? "mark" : "unmark";
        int taskNumber = parseTaskNumber(argument, commandWord);
        requireStoredTaskNumber(taskNumber, commandWord);

        Task task = tasks.getTask(taskNumber);
        if (isDone) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }
        ui.showTaskMarked(task, isDone);
    }

    /**
     * Returns the task number typed after a "mark"/"unmark" command word.
     *
     * @param argument Text after the command word.
     * @param commandWord Command word the user typed, quoted back in any error message.
     * @return Task number as shown to the user, starting at 1.
     * @throws TuringException If the text is missing or is not a whole number.
     */
    private static int parseTaskNumber(String argument, String commandWord) throws TuringException {
        String usage = "Please use: " + commandWord + " <task number>, e.g. " + commandWord + " 2";
        if (argument.isEmpty()) {
            throw new TuringException("Please tell me which task to " + commandWord + ".", usage);
        }

        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException exception) {
            // The user typed something like "mark two", or a number too large to hold.
            throw new TuringException("I need a task number, and \"" + argument + "\" is not one.", usage);
        }
    }

    /**
     * Checks that a task carrying the given number is stored.
     *
     * @param taskNumber Task number as shown to the user, starting at 1.
     * @param commandWord Command word the user typed, quoted back in any error message.
     * @throws TuringException If no stored task carries that number.
     */
    private void requireStoredTaskNumber(int taskNumber, String commandWord) throws TuringException {
        if (tasks.isEmpty()) {
            throw new TuringException("Your list is empty, so there is nothing to " + commandWord + " yet.",
                    "Add a task first, e.g. todo borrow book");
        }

        if (!tasks.hasTaskNumber(taskNumber)) {
            throw new TuringException("There is no task " + taskNumber + " in your list.",
                    "Please pick a number from 1 to " + tasks.getTaskCount() + ", or type list to see them.");
        }
    }

    /**
     * Removes the task named by a "delete" command and confirms the removal.
     *
     * @param argument Text after the command word, expected to be a task number.
     * @throws TuringException If the argument does not name a stored task.
     */
    private void deleteTask(String argument) throws TuringException {
        String commandWord = "delete";
        int taskNumber = parseTaskNumber(argument, commandWord);
        requireStoredTaskNumber(taskNumber, commandWord);

        Task removedTask = tasks.remove(taskNumber);
        ui.showTaskRemoved(removedTask, tasks.getTaskCount());
    }

    /**
     * Carries out one line of user input and reports whether the chatbot should stop.
     * Anything the user can put right is reported back to them and the
     * conversation carries on, so a mistyped command never ends the session.
     *
     * @param rawInput One line of input, exactly as the user typed it.
     * @return True if the user asked to exit.
     */
    private boolean handleInput(String rawInput) {
        try {
            return runCommand(normalizeWhitespace(rawInput));
        } catch (TuringException exception) {
            ui.showError(exception);
            return false;
        }
    }

    /**
     * Runs the command named by one line of user input.
     *
     * @param input One line of input, with its whitespace already normalized.
     * @return True if the user asked to exit.
     * @throws TuringException If the input does not name a command the chatbot can carry out.
     */
    private boolean runCommand(String input) throws TuringException {
        // A blank line is almost certainly a stray Enter, so ask again
        // instead of treating it as a command.
        if (input.isEmpty()) {
            throw new TuringException("Please type something so I know what to do.",
                    "Try one of: " + Command.getKeywords() + ".");
        }

        // Split off the first word: it names the command, and the rest is its argument.
        // The limit of 2 keeps any remaining spaces inside the argument itself.
        String[] words = input.split(" ", 2);
        String keyword = words[0];
        String argument = words.length > 1 ? words[1] : "";

        Command command = Command.fromKeyword(keyword);
        switch (command) {
        case BYE -> {
            ui.showGoodbye();
            return true;
        }
        case LIST -> ui.showTaskList(tasks);
        case TODO -> addTodo(argument);
        case DEADLINE -> addDeadline(argument);
        case EVENT -> addEvent(argument);
        case MARK -> setDoneStatus(argument, true);
        case UNMARK -> setDoneStatus(argument, false);
        case DELETE -> deleteTask(argument);
        default -> throw new TuringException("Sorry, I don't know what \"" + keyword + "\" means.",
                "Try one of: " + Command.getKeywords() + ".");
        }

        // Only reached once the command has run without complaint, so whatever
        // it changed is worth writing out before the user types the next one.
        if (command.isSaveNeeded()) {
            saveTasks();
        }
        return false;
    }

    /**
     * Fills the task list from the save file, explaining anything that went
     * wrong. A problem here is not fatal: the chatbot carries on with whatever
     * it managed to read, which on a first run is nothing at all.
     */
    private void loadTasks() {
        try {
            int skippedLineCount = storage.load(tasks);
            if (skippedLineCount > 0) {
                ui.showSkippedSaveLines(skippedLineCount);
            } else if (!tasks.isEmpty()) {
                ui.showTasksRestored(tasks.getTaskCount());
            }
        } catch (TuringException exception) {
            ui.showError(exception);
        }
    }

    /**
     * Writes the task list to the save file, explaining a failure to the user.
     * The change they just made stays in the list either way, so a save that
     * fails is worth reporting but not worth undoing the command over.
     */
    private void saveTasks() {
        try {
            storage.save(tasks);
        } catch (TuringException exception) {
            ui.showError(exception);
        }
    }

    /**
     * Runs the chatbot, reading commands from standard input until the user
     * says goodbye or the input ends.
     */
    private void run() {
        ui.showWelcome();
        loadTasks();

        while (ui.hasNextCommand()) {
            boolean shouldExit = handleInput(ui.readCommand());
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
