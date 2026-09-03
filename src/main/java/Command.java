import java.util.StringJoiner;

/**
 * The instructions the chatbot understands, each paired with the keyword the
 * user types to invoke it. Collecting them here keeps the set of valid
 * commands in one place, so the chatbot cannot fall out of step with the list
 * of commands it offers the user.
 */
public enum Command {
    /** Adds a task with no date/time, e.g. "todo borrow book". */
    TODO("todo"),

    /** Adds a task with a due date, e.g. "deadline return book /by Sunday". */
    DEADLINE("deadline"),

    /** Adds a task with a start and an end, e.g. "event meeting /from 2pm /to 4pm". */
    EVENT("event"),

    /** Shows everything stored so far. */
    LIST("list"),

    /** Marks a task as done, e.g. "mark 2". */
    MARK("mark"),

    /** Marks a task as not done, e.g. "unmark 2". */
    UNMARK("unmark"),

    /** Ends the conversation. */
    BYE("bye"),

    /** Stands for anything the chatbot does not recognize, so it has no keyword. */
    UNKNOWN("");

    /** Word the user types to invoke this command. */
    private final String keyword;

    Command(String keyword) {
        this.keyword = keyword;
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
