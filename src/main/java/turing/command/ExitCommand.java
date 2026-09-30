package turing.command;

import turing.Ui;
import turing.task.TaskList;

/** Says goodbye and ends the conversation. */
public class ExitCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui) {
        ui.showGoodbye();
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
