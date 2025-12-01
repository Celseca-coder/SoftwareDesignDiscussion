package lab1.application;

import lab1.infrastructure.IFileSystem;
import lab1.infrastructure.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WorkspaceTest {

    private Workspace workspace;

    @Mock
    private IFileSystem mockFileSystem;

    @Mock
    private Logger mockLogger;

    private LoggingService loggingService;

    @BeforeEach
    public void setUp() throws IOException {
        // 初始化 Mockito mocks
        MockitoAnnotations.openMocks(this);

        // 配置 mockFileSystem 的默认行为
        when(mockFileSystem.fileExists(anyString())).thenReturn(false);


        when(mockFileSystem.readFile(anyString())).thenReturn("");
        doNothing().when(mockFileSystem).writeFile(anyString(), anyString());

        // 配置 mockLogger 的默认行为
        doNothing().when(mockLogger).writeLog(anyString(), anyString());
        when(mockLogger.readLog(anyString())).thenReturn("Mock log content");

        // 创建真实的 LoggingService，但注入 mock Logger
        loggingService = new LoggingService(mockLogger);

        // 使用 getTestInstance 创建测试实例
        workspace = Workspace.getTestInstance(mockFileSystem, loggingService);
    }

    @Test
    public void testInitFile() throws Exception {
        workspace.initFile("test.txt", false);

        assertNotNull(workspace.getActiveEditor());
        assertEquals("test.txt", workspace.getActiveEditor().getFilePath());
        assertTrue(workspace.getActiveEditor().isModified());

        // 验证没有保存到文件系统（因为是新文件）
        verify(mockFileSystem, never()).writeFile(anyString(), anyString());
    }

    @Test
    public void testInitFileWithLog() throws Exception {
        workspace.initFile("test.txt", true);

        assertNotNull(workspace.getActiveEditor());
        assertTrue(workspace.getActiveEditor().isModified());

        // 验证日志服务被启用
        assertTrue(loggingService.isLoggingEnabled("test.txt"));

        // 验证写入了 session start 日志
        verify(mockLogger, atLeastOnce()).writeLog(contains(".test.txt.log"), contains("session start"));
    }

    @Test
    public void testSwitchActiveEditor() throws Exception {
        workspace.loadFile("test.txt");
        workspace.switchActiveEditor("test.txt");

        assertNotNull(workspace.getActiveEditor());
        assertEquals("test.txt", workspace.getActiveEditor().getFilePath());
    }

    @Test
    public void testEditorList() throws Exception {
        workspace.loadFile("file1.txt");
        workspace.loadFile("file2.txt");

        assertEquals(2, workspace.getOpenEditors().size());
        assertTrue(workspace.getOpenEditors().containsKey("file1.txt"));
        assertTrue(workspace.getOpenEditors().containsKey("file2.txt"));
    }

    @Test
    public void testDirTree() {
        IFileSystem fs = workspace.getFileSystem();
        assertNotNull(fs);
        assertSame(mockFileSystem, fs);
    }

    @Test
    public void testGetUnsavedEditors_ForExitCommand() throws Exception {
        workspace.loadFile("test.txt");
        workspace.getActiveEditor().setModified(true);

        assertEquals(1, workspace.getUnsavedEditors().size());
        assertEquals("test.txt", workspace.getUnsavedEditors().get(0).getFilePath());
    }

    @Test
    public void testShowContent_FormatsCorrectly() throws Exception {
        //  必须覆盖 setUp 的默认值，告诉 mock 文件 *确实* 存在
        when(mockFileSystem.fileExists("test.txt")).thenReturn(true);

        // 设置 readFile 的行为
        when(mockFileSystem.readFile("test.txt")).thenReturn("Line 1\nLine 2\nLine 3");

        // --- 执行 (Act) ---
        // 现在 loadFile 会 *真正* 调用 readFile
        workspace.loadFile("test.txt");

        // 获取内容
        String content = workspace.showContent(1, 3);

        // --- 断言 (Assert) ---
        assertNotNull(content);
        // 验证内容（注意：装饰器会添加行号）
        assertTrue(content.contains("1: Line 1"));
        assertTrue(content.contains("2: Line 2"));
        assertTrue(content.contains("3: Line 3"));
    }

    @Test
    public void testLogOn_DelegatesToLoggingService() {
        loggingService.enableLogging("test.txt");

        assertTrue(loggingService.isLoggingEnabled("test.txt"));
    }

    @Test
    public void testLogOff_DelegatesToLoggingService() {
        loggingService.enableLogging("test.txt");
        loggingService.disableLogging("test.txt");

        assertFalse(loggingService.isLoggingEnabled("test.txt"));
    }

    @Test
    public void testLogShow_DelegatesToLoggingService() throws IOException{
        String logContent = loggingService.getLogContent("test.txt");

        assertNotNull(logContent);
        verify(mockLogger).readLog(contains(".test.txt.log"));
    }

    @Test
    public void testLoadExistingFile() throws Exception {
        // 模拟文件已存在
        when(mockFileSystem.fileExists("existing.txt")).thenReturn(true);
        when(mockFileSystem.readFile("existing.txt")).thenReturn("Existing content");

        workspace.loadFile("existing.txt");

        assertNotNull(workspace.getActiveEditor());
        assertEquals("Existing content", workspace.getActiveEditor().getContent());
        assertFalse(workspace.getActiveEditor().isModified()); // 已存在的文件不标记为修改
    }

    @Test
    public void testSaveFile() throws Exception {
        workspace.initFile("test.txt", false);
        workspace.getActiveEditor().setModified(true);

        workspace.saveFile("test.txt");

        verify(mockFileSystem).writeFile(eq("test.txt"), anyString());
        assertFalse(workspace.getActiveEditor().isModified());
    }

    @Test
    public void testCloseFile() throws Exception {
        // 1. 准备 (Arrange):
        // 覆盖 setUp 的默认行为。我们告诉 mock，这个文件*确实*存在。
        when(mockFileSystem.fileExists("test.txt")).thenReturn(true);
        when(mockFileSystem.readFile("test.txt")).thenReturn("some content");

        // 2. 执行 (Act)
        // 现在 loadFile 会加载一个 *未修改* 的文件 (isNewFile = false)
        workspace.loadFile("test.txt");

        // 再次执行
        boolean closed = workspace.closeFile("test.txt");

        // 3. 断言 (Assert)
        assertTrue(closed); // "closed" 现在应该是 true
        assertFalse(workspace.getOpenEditors().containsKey("test.txt"));
    }

    @Test
    public void testCloseModifiedFile() throws Exception {
        workspace.loadFile("test.txt");
        workspace.getActiveEditor().setModified(true);

        boolean closed = workspace.closeFile("test.txt");

        assertFalse(closed); // 不能关闭已修改的文件
        assertTrue(workspace.getOpenEditors().containsKey("test.txt"));
    }
}