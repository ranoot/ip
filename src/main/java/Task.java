/**
 * Represents a single task in the user's list, along with whether it is done.
 * Every task has a type, so this class is abstract: the concrete subclasses
 * {@link Todo}, {@link Deadline} and {@link Event} each supply their own type
 * icon and, where they carry extra date/time information, their own display form.
 */
public abstract class Task {
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
     * Returns the single-character icon identifying the kind of task, such as
     * "T" for a todo. Each subclass decides its own icon, which is why this
     * method has no body here.
     *
     * @return Single-character type icon.
     */
    public abstract String getTypeIcon();

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
     * Returns the task formatted for display, e.g. {@code [T][X] read book}.
     * Overriding toString lets the task decide how it looks, so the chatbot
     * does not need to know about the type and status boxes at all. Subclasses
     * that carry extra information append it to this text.
     *
     * @return Display form of this task.
     */
    @Override
    public String toString() {
        return "[" + getTypeIcon() + "][" + getStatusIcon() + "] " + description;
    }
}
