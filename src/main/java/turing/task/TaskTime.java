package turing.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * A point in time attached to a task, such as when a deadline is due.
 * A date written as {@code 2019-10-15}, optionally followed by a time as
 * {@code 1800}, is understood as a real date and shown back in a friendlier
 * form, e.g. {@code Oct 15 2019 6:00PM}. Anything else the user writes, such
 * as "Sunday", is kept as it is: the point of a task list is to record what
 * the user meant, so an entry the chatbot cannot date is still worth keeping.
 */
public class TaskTime {
    /** Format accepted for a date followed by a 24-hour time, e.g. "2019-10-15 1800". */
    private static final DateTimeFormatter INPUT_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm", Locale.ENGLISH);

    // The display formats below name a locale explicitly rather than taking the
    // machine's own, so that a task reads the same way wherever the chatbot runs.

    /** Format a date is shown in, e.g. "Oct 15 2019". */
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);

    /** Format a time of day is shown in, e.g. "6:00PM". */
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH);

    /** Exactly what the user wrote, which is what gets saved. */
    private final String text;

    /** Date the text names, or null if it names no date the chatbot recognizes. */
    private final LocalDate date;

    /** Time of day the text names, or null if it gives only a date or no date at all. */
    private final LocalTime time;

    private TaskTime(String text, LocalDate date, LocalTime time) {
        this.text = text;
        this.date = date;
        this.time = time;
    }

    /**
     * Returns the point in time the given text describes, dated if the text is
     * a date the chatbot recognizes and left as written if it is not.
     *
     * @param text What the user wrote, such as "2019-10-15 1800" or "Sunday".
     * @return Point in time carrying that text.
     */
    public static TaskTime of(String text) {
        LocalDateTime dateTime = parseDateTime(text);
        if (dateTime != null) {
            return new TaskTime(text, dateTime.toLocalDate(), dateTime.toLocalTime());
        }

        LocalDate dateOnly = parseDate(text);
        if (dateOnly != null) {
            return new TaskTime(text, dateOnly, null);
        }

        // Not a date the chatbot knows how to read, so the text stands for itself.
        return new TaskTime(text, null, null);
    }

    /**
     * Returns the text to write to the save file, which is what the user wrote.
     * Saving their own words rather than a reformatted date means a save file
     * written before the chatbot understood dates still reads back correctly,
     * and a date the chatbot cannot read is not quietly lost.
     *
     * @return Text to save.
     */
    public String toSaveFormat() {
        return text;
    }

    /**
     * Returns the date the given text names, or null if it names none.
     *
     * @param text What the user wrote.
     * @return Matching date, or null if the text is not a plain date.
     */
    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    /**
     * Returns the date and time the given text names, or null if it names none.
     *
     * @param text What the user wrote.
     * @return Matching date and time, or null if the text is not a date with a time.
     */
    private static LocalDateTime parseDateTime(String text) {
        try {
            return LocalDateTime.parse(text, INPUT_DATE_TIME_FORMAT);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    /**
     * Returns this point in time as the user should see it: a recognized date
     * in a friendlier form, or otherwise exactly what they wrote.
     *
     * @return Display form of this point in time.
     */
    @Override
    public String toString() {
        if (date == null) {
            return text;
        }

        String displayedDate = date.format(DISPLAY_DATE_FORMAT);
        return time == null ? displayedDate : displayedDate + " " + time.format(DISPLAY_TIME_FORMAT);
    }
}
