package turing.task;

/**
 * Represents a task that starts and ends at a given date/time,
 * e.g. {@code project meeting (from: Oct 15 2019 2:00PM to: 4:00PM)}.
 */
public class Event extends Task {
    /** Icon identifying this kind of task, in both the display and the save file. */
    public static final String TYPE_ICON = "E";

    /** When the event starts. */
    protected final TaskTime from;

    /** When the event ends. */
    protected final TaskTime to;

    /**
     * Creates an event that starts off as not done.
     *
     * @param description What the user has to do.
     * @param from When the event starts, as the user wrote it.
     * @param to When the event ends, as the user wrote it.
     */
    public Event(String description, String from, String to) {
        super(description);
        // Both arrive as text whether the user just typed them or they came
        // back from the save file, so an event reads its own dates.
        this.from = TaskTime.of(from);
        this.to = TaskTime.of(to);
    }

    @Override
    public String getTypeIcon() {
        return TYPE_ICON;
    }

    /**
     * Returns the event in save form, e.g.
     * {@code E | 0 | project meeting | Mon 2pm | 4pm}.
     *
     * @return Save form of this event.
     */
    @Override
    public String toSaveFormat() {
        return super.toSaveFormat() + SAVE_SEPARATOR + from.toSaveFormat()
                + SAVE_SEPARATOR + to.toSaveFormat();
    }

    /**
     * Returns the event formatted for display, e.g.
     * {@code [E][ ] project meeting (from: Oct 15 2019 to: Oct 16 2019)}. The shared part of
     * the text comes from the superclass, so only the times are added here.
     *
     * @return Display form of this event.
     */
    @Override
    public String toString() {
        return super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
