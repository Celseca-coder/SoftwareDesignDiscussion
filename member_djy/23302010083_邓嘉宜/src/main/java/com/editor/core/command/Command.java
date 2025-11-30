package com.editor.core.command;

/**
 * 命令接口（Command模式）
 */
public interface Command {
    /**
     * 执行命令
     * @throws CommandException 如果命令执行失败
     */
    void execute() throws CommandException;
    
    /**
     * 获取命令名称
     * @return 命令名称
     */
    String getCommandName();
    
    /**
     * 获取命令描述
     * @return 命令描述
     */
    String getDescription();
}
