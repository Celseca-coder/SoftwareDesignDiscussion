package com.editor.core.command;

/**
 * 命令执行异常
 */
public class CommandException extends Exception {
    public CommandException(String message) {
        super(message);
    }
    
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
