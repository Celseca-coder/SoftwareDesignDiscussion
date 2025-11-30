package com.editor.core.command;

/**
 * 可撤销命令接口（Command模式）
 * 只有编辑操作才实现此接口
 */
public interface UndoableCommand extends Command {
    /**
     * 撤销命令
     * @throws CommandException 如果撤销失败
     */
    void undo() throws CommandException;
    
    /**
     * 检查是否可以撤销
     * @return true表示可以撤销
     */
    boolean isUndoable();
}
