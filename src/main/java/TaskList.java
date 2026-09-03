/**
 * Holds the tasks the user has entered, in the order they were added.
 * Task numbers used here are the ones shown to the user, which start at 1, so
 * callers never have to translate between those numbers and array indexes.
 */
public class TaskList {
    /** Upper bound on the number of tasks, as allowed by the requirements. */
    public static final int MAX_TASKS = 100;

    // A fixed-size array is enough while the requirements cap the task count at 100.

    /** Tasks stored so far, filled from index 0 upwards. */
    private final Task[] tasks = new Task[MAX_TASKS];

    /** Number of entries of tasks that are in use, which is also the next free index. */
    private int taskCount = 0;

    /**
     * Returns how many tasks are stored.
     *
     * @return Number of stored tasks.
     */
    public int getTaskCount() {
        return taskCount;
    }

    /**
     * Returns whether nothing has been stored yet.
     *
     * @return True if the list holds no tasks.
     */
    public boolean isEmpty() {
        return taskCount == 0;
    }

    /**
     * Returns whether the list has run out of room.
     *
     * @return True if no further task can be stored.
     */
    public boolean isFull() {
        return taskCount == MAX_TASKS;
    }

    /**
     * Adds a task to the end of the list. The caller is expected to have
     * checked {@link #isFull()} first.
     *
     * @param task Task to store.
     */
    public void add(Task task) {
        tasks[taskCount] = task;
        taskCount++;
    }

    /**
     * Returns whether the given number names a stored task.
     *
     * @param taskNumber Task number as shown to the user, starting at 1.
     * @return True if a task with that number exists.
     */
    public boolean hasTaskNumber(int taskNumber) {
        return taskNumber >= 1 && taskNumber <= taskCount;
    }

    /**
     * Returns the task with the given number. The caller is expected to have
     * checked {@link #hasTaskNumber(int)} first.
     *
     * @param taskNumber Task number as shown to the user, starting at 1.
     * @return Task carrying that number.
     */
    public Task getTask(int taskNumber) {
        // Task numbers shown to the user start at 1, but array indexes start at 0.
        return tasks[taskNumber - 1];
    }
}
