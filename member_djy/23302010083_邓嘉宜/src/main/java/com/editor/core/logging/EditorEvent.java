package com.editor.core.logging;

/**
 * 编辑器事件类
 * 用于观察者模式的事件通知
 */
public class EditorEvent {
    public enum EventType {
        FILE_OPENED,
        FILE_CLOSED,
        FILE_MODIFIED,
        FILE_SAVED,
        COMMAND_EXECUTED
    }
    
    private EventType eventType;
    private String filePath;
    private long timestamp;
    private String commandName;
    private String commandArgs;
    
    public EditorEvent(EventType eventType, String filePath, String commandName, String commandArgs) {
        this.eventType = eventType;
        this.filePath = filePath;
        this.timestamp = System.currentTimeMillis();
        this.commandName = commandName;
        this.commandArgs = commandArgs;
    }
    
    public EventType getEventType() {
        return eventType;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getCommandName() {
        return commandName;
    }
    
    public String getCommandArgs() {
        return commandArgs;
    }
}
