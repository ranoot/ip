/**
 * Represents a task that starts and ends at a given date/time,
 * e.g. {@code project meeting (from: Mon 2pm to: 4pm)}.
 */
public class Event extends Task {
    /** When the event starts, kept as free text such as "Mon 2pm". */
    protected String from;

    /** When the event ends, kept as free text such as "4pm". */
    protected String to;

    /**
     * Creates an event that starts off as not done.
     *
     * @param description What the user has to do.
     * @param from When the event starts.
     * @param to When the event ends.
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    @Override
    public String getTypeIcon() {
        return "E";
    }

    /**
     * Returns the event formatted for display, e.g.
     * {@code [E][ ] project meeting (from: Mon 2pm to: 4pm)}. The shared part of
     * the text comes from the superclass, so only the times are added here.
     *
     * @return Display form of this event.
     */
    @Override
    public String toString() {
        return super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
