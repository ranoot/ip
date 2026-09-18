package turing.task;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the tasks the user has entered, in the order they were added.
 * Task numbers used here are the ones shown to the user, which start at 1, so
 * callers never have to translate between those numbers and list positions.
 */
public class TaskList {
    // An ArrayList grows on demand, so the list is limited only by memory and
    // no code has to guard against running out of room.

    /** Tasks stored so far, in the order the user added them. */
    private final List<Task> tasks = new ArrayList<>();

    /**
     * Returns how many tasks are stored.
     *
     * @return Number of stored tasks.
     */
    public int getTaskCount() {
        return tasks.size();
    }

    /**
     * Returns whether nothing has been stored yet.
     *
     * @return True if the list holds no tasks.
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task Task to store.
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Returns whether the given number names a stored task.
     *
     * @param taskNumber Task number as shown to the user, starting at 1.
     * @return True if a task with that number exists.
     */
    public boolean hasTaskNumber(int taskNumber) {
        return taskNumber >= 1 && taskNumber <= getTaskCount();
    }

    /**
     * Returns the task with the given number. The caller is expected to have
     * checked {@link #hasTaskNumber(int)} first.
     *
     * @param taskNumber Task number as shown to the user, starting at 1.
     * @return Task carrying that number.
     */
    public Task getTask(int taskNumber) {
        // Task numbers shown to the user start at 1, but list positions start at 0.
        return tasks.get(taskNumber - 1);
    }

    /**
     * Removes the task with the given number and returns it. Every later task
     * moves up one place, so the numbers the user sees stay consecutive. The
     * caller is expected to have checked {@link #hasTaskNumber(int)} first.
     *
     * @param taskNumber Task number as shown to the user, starting at 1.
     * @return Task that was removed.
     */
    public Task remove(int taskNumber) {
        return tasks.remove(taskNumber - 1);
    }
}
