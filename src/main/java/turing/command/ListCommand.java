package turing.command;

import turing.Ui;
import turing.task.TaskList;

/** Shows every stored task, without changing any of them. */
public class ListCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui) {
        ui.showTaskList(tasks);
    }
}
