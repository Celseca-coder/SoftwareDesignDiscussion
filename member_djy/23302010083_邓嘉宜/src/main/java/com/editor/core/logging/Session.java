package com.editor.core.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 会话类
 * 表示一次程序运行会话
 */
public class Session {
    private LocalDateTime startTime;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public Session() {
        this.startTime = LocalDateTime.now();
    }
    
    public String getStartTimeString() {
        return startTime.format(FORMATTER);
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
}
