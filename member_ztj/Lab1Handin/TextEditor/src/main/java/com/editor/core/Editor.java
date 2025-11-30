package com.editor.core;

import com.editor.command.CommandHistory;
import com.editor.pattern.observer.Subject;

/**
 * 编辑器抽象基类
 * 继承Subject以支持观察者模式
 */
public abstract class Editor extends Subject {
    protected String filepath;
    protected boolean isModified;
    protected boolean logEnabled;
    protected CommandHistory commandHistory;
    
    public Editor(String filepath) {
        this.filepath = filepath;
        this.isModified = false;
        this.logEnabled = false;
        this.commandHistory = new CommandHistory();
    }
    
    public abstract void load() throws Exception;
    public abstract void save() throws Exception;
    public abstract String getContent();
    
    // Getters and setters
    public String getFilepath() {
        return filepath;
    }
    
    public boolean isModified() {
        return isModified;
    }
    
    public void markModified() {
        this.isModified = true;
    }
    
    public void markSaved() {
        this.isModified = false;
    }
    
    public void undo() throws Exception {
        commandHistory.undo();
        markModified();
    }
    
    public void redo() throws Exception {
        commandHistory.redo();
        markModified();
    }
    
    public boolean canUndo() {
        return commandHistory.canUndo();
    }
    
    public boolean canRedo() {
        return commandHistory.canRedo();
    }
}

