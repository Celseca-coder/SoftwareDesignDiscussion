package com.editor.exception;

/**
 * 编辑器基础异常
 */
public class EditorException extends Exception {
    public EditorException(String message) {
        super(message);
    }
    
    public EditorException(String message, Throwable cause) {
        super(message, cause);
    }
}

