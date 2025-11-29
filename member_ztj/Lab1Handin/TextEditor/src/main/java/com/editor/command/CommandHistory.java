package com.editor.command;

import java.util.Stack;

/**
 * 命令历史
 * 维护撤销和重做栈
 */
public class CommandHistory {
    private Stack<Command> undoStack;
    private Stack<Command> redoStack;
    
    public CommandHistory() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }
    
    /**
     * 执行命令并记录历史
     */
    public void execute(Command command) throws Exception {
        command.execute();
        if (command.isUndoable()) {
            undoStack.push(command);
            // 清空重做栈
            redoStack.clear();
        }
    }
    
    /**
     * 撤销
     */
    public void undo() throws Exception {
        if (undoStack.isEmpty()) {
            throw new IllegalStateException("没有可撤销的命令");
        }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
    }
    
    /**
     * 重做
     */
    public void redo() throws Exception {
        if (redoStack.isEmpty()) {
            throw new IllegalStateException("没有可重做的命令");
        }
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
    }
    
    /**
     * 是否有可撤销的命令
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    /**
     * 是否有可重做的命令
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    /**
     * 清空历史
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}

