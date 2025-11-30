package com.editor.core.command;

import java.util.Stack;

/**
 * 命令管理器
 * 维护全局命令的undo/redo栈
 * 注意：这里管理的是全局命令，每个Editor还有自己的undo/redo栈
 */
public class CommandManager {
    private Stack<UndoableCommand> undoStack;
    private Stack<UndoableCommand> redoStack;
    
    public CommandManager() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }
    
    /**
     * 执行命令
     */
    public void executeCommand(Command command) throws CommandException {
        command.execute();
        
        // 如果是可撤销命令，加入undo栈
        if (command instanceof UndoableCommand) {
            UndoableCommand undoableCommand = (UndoableCommand) command;
            if (undoableCommand.isUndoable()) {
                undoStack.push(undoableCommand);
                // 新的操作清除redo栈
                redoStack.clear();
            }
        }
    }
    
    /**
     * 撤销上一个命令
     */
    public void undo() throws CommandException {
        if (undoStack.isEmpty()) {
            throw new CommandException("没有可撤销的操作");
        }
        
        UndoableCommand command = undoStack.pop();
        command.undo();
        redoStack.push(command);
    }
    
    /**
     * 重做上一个撤销的命令
     */
    public void redo() throws CommandException {
        if (redoStack.isEmpty()) {
            throw new CommandException("没有可重做的操作");
        }
        
        UndoableCommand command = redoStack.pop();
        command.execute();
        undoStack.push(command);
    }
    
    /**
     * 检查是否可以撤销
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    /**
     * 检查是否可以重做
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    /**
     * 清空命令历史
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
