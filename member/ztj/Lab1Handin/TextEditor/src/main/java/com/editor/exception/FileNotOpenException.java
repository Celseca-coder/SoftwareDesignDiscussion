package com.editor.exception;

/**
 * 文件未打开异常
 */
public class FileNotOpenException extends EditorException {
    public FileNotOpenException(String message) {
        super(message);
    }
}

