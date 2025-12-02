package lab1.application;

import lab1.application.event.CommandExecutedEvent;
import lab1.infrastructure.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 目标：单元测试 LoggingService 的 *业务逻辑*
 * 验证：是否能正确地启用/禁用日志，以及是否能正确响应事件
 * TDD 原则：使用 Mock Logger 来隔离基础设施
 */
class LoggingServiceTest {

    @Mock
    private Logger mockLogger; // 唯一的依赖

    private LoggingService loggingService;
    private final String TEST_FILE = "test.txt";

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        // 注入 Mock Logger
        loggingService = new LoggingService(mockLogger);
        // 模拟 readLog 的行为，否则会抛出 IOException
        when(mockLogger.readLog(anyString())).thenReturn("log content");
    }

    @Test
    void testEvent_WhenLogIsOn() {
        // 准备 (Arrange): 启用日志
        loggingService.enableLogging(TEST_FILE);
        assertTrue(loggingService.isLoggingEnabled(TEST_FILE));

        CommandExecutedEvent event = new CommandExecutedEvent(TEST_FILE, "append \"test\"");

        // 执行 (Act): 发布一个事件
        loggingService.onEvent(event);

        // 断言 (Assert): Logger *必须* 被调用
        verify(mockLogger, times(1)).writeLog(contains(".test.txt.log"), contains("append \"test\""));
    }

    @Test
    void testEvent_WhenLogIsOff() {
        // 准备 (Arrange): 确保日志是关闭的
        assertFalse(loggingService.isLoggingEnabled(TEST_FILE));
        CommandExecutedEvent event = new CommandExecutedEvent(TEST_FILE, "append \"test\"");

        // 执行 (Act): 发布一个事件
        loggingService.onEvent(event);

        // 断言 (Assert): Logger *绝不* 被调用
        verify(mockLogger, never()).writeLog(anyString(), anyString());
    }

    @Test
    void testGetLogFilePath() {
        // 验证日志文件名生成的逻辑
        String logPath = loggingService.getLogFilePath("dir/file.txt");
        assertEquals("dir" + File.separator + ".file.txt.log", logPath);

        String logPathRoot = loggingService.getLogFilePath("file.txt");
        assertEquals(".file.txt.log", logPathRoot);
    }

    @Test
    void testLogShow_DelegatesToLogger() throws IOException {
        loggingService.getLogContent(TEST_FILE);
        // 验证 getLogContent 是否正确调用了 mockLogger.readLog
        verify(mockLogger, times(1)).readLog(eq(".test.txt.log"));
    }
}
