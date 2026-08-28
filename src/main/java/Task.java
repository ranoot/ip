/**
 * Represents a single task in the user's list, along with whether it is done.
 */
public class Task {
    /** What the user has to do, exactly as typed. */
    protected String description;

    /** True once the task has been marked as done. */
    protected boolean isDone;

    /**
     * Creates a task that starts off as not done.
     *
     * @param description What the user has to do.
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the icon shown inside the status box: "X" when done, a space otherwise.
     * Using a space keeps every task the same width, so the list stays aligned.
     *
     * @return Single-character status icon.
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /** Marks this task as done. */
    public void markAsDone() {
        this.isDone = true;
    }

    /** Marks this task as not done yet. */
    public void markAsNotDone() {
        this.isDone = false;
    }

    /**
     * Returns the task formatted for display, e.g. {@code [X] read book}.
     * Overriding toString lets the task decide how it looks, so the chatbot
     * does not need to know about the status box at all.
     *
     * @return Display form of this task.
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
