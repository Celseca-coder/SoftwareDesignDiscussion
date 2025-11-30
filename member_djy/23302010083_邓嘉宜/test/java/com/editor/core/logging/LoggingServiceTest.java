package com.editor.core.logging;

import com.editor.core.persistence.FilePersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * LoggingService 单元测试
 * 测试日志服务的所有功能
 */
public class LoggingServiceTest {
    private LoggingService loggingService;
    private FilePersistence filePersistence;
    
    @BeforeEach
    void setUp() {
        filePersistence = new FilePersistence();
        loggingService = new LoggingService(filePersistence);
    }
    
    // ========== 日志开关测试 ==========
    
    /**
     * 测试启用日志。
     * 测试数据：文件路径 "test.txt"。
     * 预期：isLoggingEnabled 返回 true。
     */
    @Test
    void testEnableLogging() {
        String filePath = "test.txt";
        loggingService.enableLogging(filePath);
        assertTrue(loggingService.isLoggingEnabled(filePath));
    }
    
    /**
     * 测试关闭日志。
     * 测试数据：先启用 "test.txt" 的日志，然后关闭。
     * 预期：isLoggingEnabled 返回 false。
     */
    @Test
    void testDisableLogging() {
        String filePath = "test.txt";
        loggingService.enableLogging(filePath);
        loggingService.disableLogging(filePath);
        assertFalse(loggingService.isLoggingEnabled(filePath));
    }
    
    /**
     * 测试默认日志状态。
     * 测试数据：文件路径 "test.txt"，未启用日志。
     * 预期：isLoggingEnabled 返回 false。
     */
    @Test
    void testLoggingDisabledByDefault() {
        String filePath = "test.txt";
        assertFalse(loggingService.isLoggingEnabled(filePath));
    }
    
    // ========== 事件处理测试 ==========
    
    /**
     * 测试启用日志时的事件处理。
     * 测试数据：文件 "test_log.txt"，启用日志，发送 COMMAND_EXECUTED 事件。
     * 预期：日志文件 ".test_log.txt.log" 被创建。
     */
    @Test
    void testOnEventWithLoggingEnabled() throws IOException {
        String filePath = "test_log.txt";
        String logFilePath = ".test_log.txt.log";
        
        // 清理可能存在的日志文件
        Path logPath = Paths.get(logFilePath);
        if (Files.exists(logPath)) {
            Files.delete(logPath);
        }
        
        loggingService.enableLogging(filePath);
        
        EditorEvent event = new EditorEvent(
            EditorEvent.EventType.COMMAND_EXECUTED,
            filePath,
            "append",
            "\"test\""
        );
        
        loggingService.onEvent(event);
        
        // 验证日志文件已创建
        assertTrue(Files.exists(logPath));
        
        // 清理
        if (Files.exists(logPath)) {
            Files.delete(logPath);
        }
    }
    
    /**
     * 测试未启用日志时的事件处理。
     * 测试数据：文件 "test.txt"，未启用日志，发送 COMMAND_EXECUTED 事件。
     * 预期：不抛出异常。
     */
    @Test
    void testOnEventWithLoggingDisabled() {
        String filePath = "test.txt";
        
        EditorEvent event = new EditorEvent(
            EditorEvent.EventType.COMMAND_EXECUTED,
            filePath,
            "append",
            "\"test\""
        );
        
        // 日志未启用，不应该抛出异常
        assertDoesNotThrow(() -> {
            loggingService.onEvent(event);
        });
    }
    
    /**
     * 测试 null 文件路径的事件处理。
     * 测试数据：filePath 为 null，发送 COMMAND_EXECUTED 事件。
     * 预期：不抛出异常。
     */
    @Test
    void testOnEventWithNullFilePath() {
        EditorEvent event = new EditorEvent(
            EditorEvent.EventType.COMMAND_EXECUTED,
            null,
            "append",
            "\"test\""
        );
        
        // 应该不抛出异常
        assertDoesNotThrow(() -> {
            loggingService.onEvent(event);
        });
    }
    
    // ========== 日志读取测试 ==========
    
    /**
     * 测试读取不存在的日志文件。
     * 测试数据：文件 "nonexistent.txt"，日志文件不存在。
     * 预期：返回空列表。
     */
    @Test
    void testReadLogFileNotExists() {
        String filePath = "nonexistent.txt";
        List<String> logLines = loggingService.readLog(filePath);
        assertTrue(logLines.isEmpty());
    }
    
    // ========== 事件类型测试 ==========
    
    /**
     * 测试不同事件类型的处理。
     * 测试数据：文件 "test.txt"，启用日志，发送 FILE_OPENED、FILE_SAVED、FILE_MODIFIED 事件。
     * 预期：不抛出异常。
     */
    @Test
    void testDifferentEventTypes() {
        String filePath = "test.txt";
        loggingService.enableLogging(filePath);
        
        EditorEvent event1 = new EditorEvent(
            EditorEvent.EventType.FILE_OPENED,
            filePath,
            "load",
            filePath
        );
        
        EditorEvent event2 = new EditorEvent(
            EditorEvent.EventType.FILE_SAVED,
            filePath,
            "save",
            filePath
        );
        
        EditorEvent event3 = new EditorEvent(
            EditorEvent.EventType.FILE_MODIFIED,
            filePath,
            "edit",
            ""
        );
        
        // 应该不抛出异常
        assertDoesNotThrow(() -> {
            loggingService.onEvent(event1);
            loggingService.onEvent(event2);
            loggingService.onEvent(event3);
        });
    }
}
