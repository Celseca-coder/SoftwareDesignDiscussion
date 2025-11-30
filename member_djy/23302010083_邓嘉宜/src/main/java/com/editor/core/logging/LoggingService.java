package com.editor.core.logging;

import com.editor.core.persistence.FilePersistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志服务类
 * 负责记录命令执行日志
 */
public class LoggingService implements EventListener {
    private Map<String, Boolean> loggingEnabled; // 文件路径 -> 是否启用日志
    private Map<String, Session> sessions; // 文件路径 -> 会话
    private FilePersistence filePersistence;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public LoggingService(FilePersistence filePersistence) {
        this.loggingEnabled = new HashMap<>();
        this.sessions = new HashMap<>();
        this.filePersistence = filePersistence;
    }
    
    @Override
    public void onEvent(EditorEvent event) {
        String filePath = event.getFilePath();
        
        // 只记录启用日志的文件的事件
        if (filePath != null && isLoggingEnabled(filePath)) {
            try {
                logEvent(filePath, event);
            } catch (IOException e) {
                // 日志写入失败仅警告，不中断程序
                System.err.println("警告: 日志写入失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 启用文件的日志记录
     * @param filePath 文件路径
     */
    public void enableLogging(String filePath) {
        loggingEnabled.put(filePath, true);
        
        // 如果还没有会话，创建新会话
        if (!sessions.containsKey(filePath)) {
            sessions.put(filePath, new Session());
            
            // 写入会话开始标记
            try {
                String logPath = getLogFilePath(filePath);
                String sessionStart = "\n=== Session started at " + 
                    LocalDateTime.now().format(FORMATTER) + " ===\n";
                appendToLogFile(logPath, sessionStart);
            } catch (IOException e) {
                System.err.println("警告: 日志写入失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 关闭文件的日志记录
     * @param filePath 文件路径
     */
    public void disableLogging(String filePath) {
        loggingEnabled.put(filePath, false);
    }
    
    /**
     * 检查文件是否启用日志
     * @param filePath 文件路径
     * @return true表示启用日志
     */
    public boolean isLoggingEnabled(String filePath) {
        Boolean enabled = loggingEnabled.get(filePath);
        if (enabled != null) {
            return enabled;
        }
        
        // 检查文件首行是否为 "# log"
        if (filePersistence.isLogEnabled(filePath)) {
            enableLogging(filePath);
            return true;
        }
        
        return false;
    }
    
    /**
     * 记录事件到日志文件
     */
    private void logEvent(String filePath, EditorEvent event) throws IOException {
        String logPath = getLogFilePath(filePath);
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry;
        
        if (event.getEventType() == EditorEvent.EventType.COMMAND_EXECUTED) {
            logEntry = String.format("[%s] %s %s\n", 
                timestamp, 
                event.getCommandName(),
                event.getCommandArgs() != null ? event.getCommandArgs() : "");
        } else {
            logEntry = String.format("[%s] %s\n", timestamp, event.getEventType().name());
        }
        
        appendToLogFile(logPath, logEntry);
    }
    
    /**
     * 追加内容到日志文件
     */
    private void appendToLogFile(String logPath, String content) throws IOException {
        Path path = Paths.get(logPath);
        
        // 如果文件不存在，创建它
        if (!Files.exists(path)) {
            // 只有当父目录存在时才创建目录（避免NullPointerException）
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.createFile(path);
        }
        
        // 追加内容
        Files.write(path, content.getBytes(StandardCharsets.UTF_8), 
            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
    
    /**
     * 获取日志文件路径
     * 格式: .filename.log (与源文件同目录)
     */
    private String getLogFilePath(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        Path parent = path.getParent();
        
        if (parent != null) {
            return parent.resolve("." + fileName + ".log").toString();
        } else {
            return "." + fileName + ".log";
        }
    }
    
    /**
     * 读取日志文件内容
     * @param filePath 源文件路径
     * @return 日志内容列表
     */
    public java.util.List<String> readLog(String filePath) {
        try {
            String logPath = getLogFilePath(filePath);
            Path path = Paths.get(logPath);
            if (Files.exists(path)) {
                return Files.readAllLines(path, StandardCharsets.UTF_8);
            } else {
                return new java.util.ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("警告: 读取日志失败: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 初始化文件日志（如果文件首行是 "# log"）
     * @param filePath 文件路径
     */
    public void initializeLoggingIfNeeded(String filePath) {
        if (filePersistence.isLogEnabled(filePath)) {
            enableLogging(filePath);
        }
    }
}
