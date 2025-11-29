package com.editor.logging;

import com.editor.pattern.observer.Observer;
import com.editor.pattern.observer.Event;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 日志管理器
 * 实现观察者模式，监听系统事件并记录日志
 */
public class Logger implements Observer {
    private String logFile;
    private boolean consoleOutput;
    private LogEvent.LogLevel minLevel;
    
    public Logger(String logFile, boolean consoleOutput, LogEvent.LogLevel minLevel) {
        this.logFile = logFile;
        this.consoleOutput = consoleOutput;
        this.minLevel = minLevel;
    }
    
    public Logger() {
        this("editor.log", true, LogEvent.LogLevel.INFO);
    }
    
    @Override
    public void update(Event event) {
        log(LogEvent.LogLevel.INFO, "Event: " + event.toString(), event.getType());
    }
    
    /**
     * 记录日志
     */
    public void log(LogEvent.LogLevel level, String message, String source) {
        LogEvent event = new LogEvent(level, message, source);
        
        // 过滤日志级别
        if (shouldLog(level)) {
            String logEntry = event.toString();
            
            // 控制台输出
            if (consoleOutput) {
                System.out.println(logEntry);
            }
            
            // 文件输出
            if (logFile != null && !logFile.isEmpty()) {
                writeToFile(logEntry);
            }
        }
    }
    
    /**
     * 判断是否应该记录该级别的日志
     */
    private boolean shouldLog(LogEvent.LogLevel level) {
        return level.ordinal() >= minLevel.ordinal();
    }
    
    /**
     * 写入文件
     */
    private void writeToFile(String entry) {
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(entry + "\n");
        } catch (IOException e) {
            System.err.println("写入日志文件失败: " + e.getMessage());
        }
    }
    
    public void debug(String message, String source) {
        log(LogEvent.LogLevel.DEBUG, message, source);
    }
    
    public void info(String message, String source) {
        log(LogEvent.LogLevel.INFO, message, source);
    }
    
    public void warn(String message, String source) {
        log(LogEvent.LogLevel.WARN, message, source);
    }
    
    public void error(String message, String source) {
        log(LogEvent.LogLevel.ERROR, message, source);
    }
}

