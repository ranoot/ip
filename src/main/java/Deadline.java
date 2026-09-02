/**
 * Represents a task that has to be done before a given date/time,
 * e.g. {@code return book (by: Sunday)}.
 */
public class Deadline extends Task {
    /** When the task is due, kept as free text such as "Sunday" or "11/10/2019 5pm". */
    protected String by;

    /**
     * Creates a deadline that starts off as not done.
     *
     * @param description What the user has to do.
     * @param by When the task is due.
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    @Override
    public String getTypeIcon() {
        return "D";
    }

    /**
     * Returns the deadline formatted for display, e.g.
     * {@code [D][ ] return book (by: Sunday)}. The shared part of the text comes
     * from the superclass, so only the due date is added here.
     *
     * @return Display form of this deadline.
     */
    @Override
    public String toString() {
        return super.toString() + " (by: " + by + ")";
    }
}
