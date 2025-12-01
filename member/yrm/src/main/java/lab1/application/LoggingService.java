package lab1.application;

// LoggingService.java

import lab1.application.event.*;
import lab1.infrastructure.Logger;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import lab1.application.event.EventListener;
import java.time.format.DateTimeFormatter;

public class LoggingService implements EventListener<CommandExecutedEvent> {
    private Set<String> enabledFiles;
    private final Logger logger; // 依赖注入的 Logger 实例

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

    // 构造函数现在接收 Logger
    public LoggingService(Logger logger) {
        this.enabledFiles = new HashSet<>();
        this.logger = logger; //  保存实例
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
            String timestamp = this.formatTimestamp(LocalDateTime.now()); // 调用本地方法
            String logEntry = timestamp + " " + event.getCommand();
            logger.writeLog(logFile, logEntry); // 调用非静态方法
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
            return logger.readLog(getLogFilePath(filePath));
        } catch (Exception e) {
            return "读取日志失败: " + e.getMessage();
        }
    }

    public void setEnabledFiles(Set<String> files) {
        this.enabledFiles = new HashSet<>(files);
    }
    // 供 Workspace 调用
    public void logSessionStart(String filePath) {
        String logFile = getLogFilePath(filePath);
        String timestamp = this.formatTimestamp(LocalDateTime.now());
        String logEntry = "session start at " + timestamp;
        logger.writeLog(logFile, logEntry);
    }

    // 格式化时间戳
    private String formatTimestamp(LocalDateTime time) {
        return time.format(formatter);
    }
}