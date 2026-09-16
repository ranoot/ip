package turing;

import java.util.StringJoiner;

/**
 * The instructions the chatbot understands, each paired with the keyword the
 * user types to invoke it. Collecting them here keeps the set of valid
 * commands in one place, so the chatbot cannot fall out of step with the list
 * of commands it offers the user.
 */
public enum Command {
    /** Adds a task with no date/time, e.g. "todo borrow book". */
    TODO("todo", true),

    /** Adds a task with a due date, e.g. "deadline return book /by Sunday". */
    DEADLINE("deadline", true),

    /** Adds a task with a start and an end, e.g. "event meeting /from 2pm /to 4pm". */
    EVENT("event", true),

    /** Shows everything stored so far. */
    LIST("list", false),

    /** Marks a task as done, e.g. "mark 2". */
    MARK("mark", true),

    /** Marks a task as not done, e.g. "unmark 2". */
    UNMARK("unmark", true),

    /** Ends the conversation. */
    BYE("bye", false),

    /** Stands for anything the chatbot does not recognize, so it has no keyword. */
    UNKNOWN("", false);

    /** Word the user types to invoke this command. */
    private final String keyword;

    /** True if carrying out this command can change the task list. */
    private final boolean isSaveNeeded;

    Command(String keyword, boolean isSaveNeeded) {
        this.keyword = keyword;
        this.isSaveNeeded = isSaveNeeded;
    }

    /**
     * Returns whether the task list has to be written to disk after this
     * command runs. Recording it here means a command added later cannot be
     * forgotten by whichever code decides when to save.
     *
     * @return True if this command can change the task list.
     */
    public boolean isSaveNeeded() {
        return isSaveNeeded;
    }

    /**
     * Returns the command invoked by the given word. Keywords are matched
     * without regard to capitalization, so "BYE" and "bye" name the same command.
     *
     * @param keyword First word of the user's input.
     * @return Matching command, or UNKNOWN if no command uses that word.
     */
    public static Command fromKeyword(String keyword) {
        for (Command command : values()) {
            if (command != UNKNOWN && command.keyword.equalsIgnoreCase(keyword)) {
                return command;
            }
        }
        return UNKNOWN;
    }

    /**
     * Returns the keywords of every command the user can type, in the order the
     * commands are declared, e.g. "todo, deadline, event, list, mark, unmark, bye".
     *
     * @return Comma separated keywords.
     */
    public static String getKeywords() {
        StringJoiner keywords = new StringJoiner(", ");
        for (Command command : values()) {
            if (command != UNKNOWN) {
                keywords.add(command.keyword);
            }
        }
        return keywords.toString();
    }
}
