package com.editor.command;

/**
 * 命令接口
 * 实现命令模式，支持执行、撤销和重做功能
 */
public interface Command {
    /**
     * 执行命令
     */
    void execute() throws Exception;
    
    /**
     * 撤销命令
     */
    void undo() throws Exception;
    
    /**
     * 是否可撤销
     */
    boolean isUndoable();
    
    /**
     * 获取命令描述
     */
    String getDescription();
}

