package turing;

import turing.task.Task;
import turing.task.TaskList;

/**
 * Entry point of the Turing chatbot.
 * Supports greeting the user, adding todos, deadlines and events, listing the
 * stored tasks, marking them as done or not done, and exiting when the user
 * types "bye".
 */
public class Turing {
    /** Where the tasks are kept between runs, relative to where the chatbot is started. */
    private static final String SAVE_FILE_PATH = "data/turing.txt";

    /** Talks to the user: reads their commands and shows them every reply. */
    private final Ui ui = new Ui();

    /** Tasks entered so far. */
    private final TaskList tasks = new TaskList();

    /** Reads and writes the save file holding those tasks. */
    private final Storage storage = new Storage(SAVE_FILE_PATH);

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
        int taskNumber = Parser.parseTaskNumber(argument, commandWord);
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
        int taskNumber = Parser.parseTaskNumber(argument, commandWord);
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
            return runCommand(Parser.normalizeWhitespace(rawInput));
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

        String keyword = Parser.parseKeyword(input);
        String argument = Parser.parseArgument(input);

        Command command = Command.fromKeyword(keyword);
        switch (command) {
        case BYE -> {
            ui.showGoodbye();
            return true;
        }
        case LIST -> ui.showTaskList(tasks);
        case TODO -> addTask(Parser.parseTodo(argument));
        case DEADLINE -> addTask(Parser.parseDeadline(argument));
        case EVENT -> addTask(Parser.parseEvent(argument));
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
