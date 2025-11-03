package lab1.application;

// LoggingService.java

import lab1.application.event.*;
import lab1.infrastructure.Logger;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import lab1.application.event.EventListener;

public class LoggingService implements EventListener<CommandExecutedEvent> {
    private Set<String> enabledFiles;

    public LoggingService() {
        this.enabledFiles = new HashSet<>();
        EventBus.getInstance().subscribe(CommandExecutedEvent.class, this);
    }

    public void enableLogging(String filePath) {
        enabledFiles.add(filePath);
    }

    public void disableLogging(String filePath) {
        enabledFiles.remove(filePath);
    }

    public boolean isLoggingEnabled(String filePath) {
        return enabledFiles.contains(filePath);
    }

    public Set<String> getEnabledFiles() {
        return new HashSet<>(enabledFiles);
    }

    @Override
    public void onEvent(CommandExecutedEvent event) {
        if (enabledFiles.contains(event.getFilePath())) {
            String logFile = getLogFilePath(event.getFilePath());
            String timestamp = Logger.formatTimestamp(LocalDateTime.now());
            String logEntry = timestamp + " " + event.getCommand();
            Logger.writeLog(logFile, logEntry);
        }
    }

    public String getLogFilePath(String filePath) {
        // 简单的实现：在文件名（不含路径）前加 "." 并添加 ".log"
        File f = new File(filePath);
        String dir = f.getParent();
        String filename = f.getName();
        String logFilename = "." + filename + ".log";

        return (dir == null) ? logFilename : dir + File.separator + logFilename;
    }

    public String getLogContent(String filePath) {
        try {
            return Logger.readLog(getLogFilePath(filePath));
        } catch (Exception e) {
            return "读取日志失败: " + e.getMessage();
        }
    }

    public void setEnabledFiles(Set<String> files) {
        this.enabledFiles = new HashSet<>(files);
    }
}