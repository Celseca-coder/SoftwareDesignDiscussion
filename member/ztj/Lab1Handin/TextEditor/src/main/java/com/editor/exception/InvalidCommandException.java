package com.editor.exception;

/**
 * 无效命令异常
 */
public class InvalidCommandException extends EditorException {
    public InvalidCommandException(String message) {
        super(message);
    }
}

