package lab1.domain.command;

import java.util.Stack;

public class CommandHistory {
    private Stack<ICommand> undoStack;
    private Stack<ICommand> redoStack;

    public CommandHistory() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    public void push(ICommand command) {
        undoStack.push(command);
        redoStack.clear();
    }

    public ICommand popUndo() {
        return undoStack.pop();
    }

    public ICommand popRedo() {
        return redoStack.pop();
    }

    public void pushUndo(ICommand command) {
        undoStack.push(command);
    }

    public void pushRedo(ICommand command) {
        redoStack.push(command);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
