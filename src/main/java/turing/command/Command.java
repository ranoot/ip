package turing.command;

import turing.TuringException;
import turing.Ui;
import turing.task.TaskList;

/**
 * One instruction the user has given, ready to be carried out.
 * Every command the chatbot understands is a subclass, and each one knows
 * both what it changes and what it tells the user afterwards. Turing can
 * therefore run any command without knowing which one it is, and adding a
 * command means adding a class rather than another branch to an existing
 * method.
 */
public abstract class Command {
    /**
     * Carries this command out.
     *
     * @param tasks Task list the command works on.
     * @param ui Voice the command reports its outcome through.
     * @throws TuringException If the command cannot be carried out as typed.
     */
    public abstract void execute(TaskList tasks, Ui ui) throws TuringException;

    /**
     * Returns whether the conversation ends after this command. Only the exit
     * command overrides this, so every other command carries on as usual.
     *
     * @return True if the chatbot should stop reading input.
     */
    public boolean isExit() {
        return false;
    }

    /**
     * Returns whether this command can change the task list, and so whether
     * the list has to be written out once it has run. Commands that only look
     * at the list leave this alone.
     *
     * @return True if the task list may have changed.
     */
    public boolean isSaveNeeded() {
        return false;
    }

    /**
     * Checks that a task carrying the given number is stored, which the
     * commands naming a task have to do before they can act on it.
     *
     * @param tasks Task list to look in.
     * @param taskNumber Task number as shown to the user, starting at 1.
     * @param commandWord Command word the user typed, quoted back in any error message.
     * @throws TuringException If no stored task carries that number.
     */
    protected static void requireStoredTaskNumber(TaskList tasks, int taskNumber, String commandWord)
            throws TuringException {
        if (tasks.isEmpty()) {
            throw new TuringException("Your list is empty, so there is nothing to " + commandWord + " yet.",
                    "Add a task first, e.g. todo borrow book");
        }

        if (!tasks.hasTaskNumber(taskNumber)) {
            throw new TuringException("There is no task " + taskNumber + " in your list.",
                    "Please pick a number from 1 to " + tasks.getTaskCount() + ", or type list to see them.");
        }
    }
}
