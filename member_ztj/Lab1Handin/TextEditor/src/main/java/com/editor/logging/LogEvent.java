package com.editor.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志事件
 */
public class LogEvent {
    private LocalDateTime timestamp;
    private LogLevel level;
    private String message;
    private String source;
    
    public LogEvent(LogLevel level, String message, String source) {
        this.timestamp = LocalDateTime.now();
        this.level = level;
        this.message = message;
        this.source = source;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public LogLevel getLevel() {
        return level;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getSource() {
        return source;
    }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] [%s] [%s] %s",
            timestamp.format(formatter),
            level,
            source,
            message
        );
    }
    
    /**
     * 日志级别枚举
     */
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
}

