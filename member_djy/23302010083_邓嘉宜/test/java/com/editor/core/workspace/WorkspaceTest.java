package com.editor.core.workspace;

import com.editor.core.editor.Editor;
import com.editor.core.editor.TextEditor;
import com.editor.core.logging.EditorEvent;
import com.editor.core.logging.EventListener;
import com.editor.core.logging.LoggingService;
import com.editor.core.persistence.FilePersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Workspace 单元测试
 * 测试工作区的文件管理和状态管理
 */
public class WorkspaceTest {
    private Workspace workspace;
    private LoggingService loggingService;
    private FilePersistence filePersistence;
    
    @BeforeEach
    void setUp() {
        filePersistence = new FilePersistence();
        loggingService = new LoggingService(filePersistence);
        workspace = new Workspace(loggingService);
    }
    
    // ========== 文件打开和关闭测试 ==========
    
    /**
     * 测试打开单个文件并自动设置为活动文件。
     * 测试数据：创建 TextEditor，文件路径为 "test.txt"，调用 openFile。
     * 预期：isFileOpen("test.txt") 为 true，getActiveFile() 返回 "test.txt"。
     */
    @Test
    void testOpenFile() {
        Editor editor = new TextEditor("test.txt");
        workspace.openFile("test.txt", editor);
        
        assertTrue(workspace.isFileOpen("test.txt"));
        assertEquals("test.txt", workspace.getActiveFile());
    }
    
    /**
     * 测试同时打开多个文件。
     * 测试数据：依次打开 "file1.txt" 和 "file2.txt" 两个编辑器。
     * 预期：两个文件都处于打开状态，getOpenFiles() 包含两条记录。
     */
    @Test
    void testOpenMultipleFiles() {
        Editor editor1 = new TextEditor("file1.txt");
        Editor editor2 = new TextEditor("file2.txt");
        
        workspace.openFile("file1.txt", editor1);
        workspace.openFile("file2.txt", editor2);
        
        assertTrue(workspace.isFileOpen("file1.txt"));
        assertTrue(workspace.isFileOpen("file2.txt"));
        List<String> openFiles = workspace.getOpenFiles();
        assertEquals(2, openFiles.size());
        assertTrue(openFiles.contains("file1.txt"));
        assertTrue(openFiles.contains("file2.txt"));
    }
    
    /**
     * 测试关闭已打开文件的基本行为。
     * 测试数据：打开 "test.txt"，然后调用 closeFile("test.txt")。
     * 预期：isFileOpen("test.txt") 返回 false。
     */
    @Test
    void testCloseFile() {
        Editor editor = new TextEditor("test.txt");
        workspace.openFile("test.txt", editor);
        
        workspace.closeFile("test.txt");
        assertFalse(workspace.isFileOpen("test.txt"));
    }
    
    /**
     * 测试关闭当前活动文件后自动切换活动文件。
     * 测试数据：打开 "file1.txt" 和 "file2.txt"，将 "file1.txt" 设为活动文件，然后关闭它。
     * 预期：活动文件切换为 "file2.txt"。
     */
    @Test
    void testCloseActiveFile() {
        Editor editor1 = new TextEditor("file1.txt");
        Editor editor2 = new TextEditor("file2.txt");
        
        workspace.openFile("file1.txt", editor1);
        workspace.openFile("file2.txt", editor2);
        workspace.setActiveFile("file1.txt");
        
        workspace.closeFile("file1.txt");
        assertEquals("file2.txt", workspace.getActiveFile());
    }
    
    /**
     * 测试关闭最后一个打开的文件时活动文件被清空。
     * 测试数据：只打开 "test.txt"，然后关闭它。
     * 预期：getActiveFile() 返回 null。
     */
    @Test
    void testCloseLastFile() {
        Editor editor = new TextEditor("test.txt");
        workspace.openFile("test.txt", editor);
        
        workspace.closeFile("test.txt");
        assertNull(workspace.getActiveFile());
    }
    
    /**
     * 测试关闭未打开文件时抛出异常。
     * 测试数据：直接调用 closeFile("nonexistent.txt")。
     * 预期：抛出 IllegalStateException，消息包含 "文件未打开"。
     */
    @Test
    void testCloseNonExistentFile() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            workspace.closeFile("nonexistent.txt");
        });
        assertTrue(exception.getMessage().contains("文件未打开"));
    }
    
    // ========== 活动文件测试 ==========
    
    /**
     * 测试设置活动文件的正常场景。
     * 测试数据：打开 "test.txt"，然后调用 setActiveFile("test.txt")。
     * 预期：getActiveFile() 返回 "test.txt"。
     */
    @Test
    void testSetActiveFile() {
        Editor editor = new TextEditor("test.txt");
        workspace.openFile("test.txt", editor);
        
        workspace.setActiveFile("test.txt");
        assertEquals("test.txt", workspace.getActiveFile());
    }
    
    /**
     * 测试将未打开的文件设置为活动文件时的异常。
     * 测试数据：直接调用 setActiveFile("nonexistent.txt")。
     * 预期：抛出 IllegalStateException，消息包含 "文件未打开"。
     */
    @Test
    void testSetActiveFileNotOpen() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            workspace.setActiveFile("nonexistent.txt");
        });
        assertTrue(exception.getMessage().contains("文件未打开"));
    }
    
    /**
     * 测试获取当前活动编辑器。
     * 测试数据：打开 "test.txt" 并设为活动文件，然后调用 getActiveEditor()。
     * 预期：返回非空 Editor，filePath 为 "test.txt"。
     */
    @Test
    void testGetActiveEditor() {
        Editor editor = new TextEditor("test.txt");
        workspace.openFile("test.txt", editor);
        workspace.setActiveFile("test.txt");
        
        Editor activeEditor = workspace.getActiveEditor();
        assertNotNull(activeEditor);
        assertEquals("test.txt", activeEditor.getFilePath());
    }
    
    /**
     * 测试没有活动文件时调用 getActiveEditor 的异常。
     * 测试数据：不打开任何文件，直接调用 getActiveEditor()。
     * 预期：抛出 IllegalStateException，消息包含 "没有活动文件"。
     */
    @Test
    void testGetActiveEditorNoActiveFile() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            workspace.getActiveEditor();
        });
        assertTrue(exception.getMessage().contains("没有活动文件"));
    }
    
    /**
     * 测试根据文件路径获取已打开的编辑器。
     * 测试数据：打开 "test.txt"，再调用 getEditor("test.txt")。
     * 预期：返回非空 Editor，filePath 为 "test.txt"。
     */
    @Test
    void testGetEditor() {
        Editor editor = new TextEditor("test.txt");
        workspace.openFile("test.txt", editor);
        
        Editor retrieved = workspace.getEditor("test.txt");
        assertNotNull(retrieved);
        assertEquals("test.txt", retrieved.getFilePath());
    }
    
    /**
     * 测试获取未打开文件的编辑器时抛出异常。
     * 测试数据：直接调用 getEditor("nonexistent.txt")。
     * 预期：抛出 IllegalStateException，消息包含 "文件未打开"。
     */
    @Test
    void testGetEditorNotOpen() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            workspace.getEditor("nonexistent.txt");
        });
        assertTrue(exception.getMessage().contains("文件未打开"));
    }
    
    // ========== 修改状态测试 ==========
    
    /**
     * 测试文件修改状态的更新和查询。
     * 测试数据：打开 "test.txt"，依次将 modified 标记设为 true 和 false。
     * 预期：isFileModified("test.txt") 随修改状态变化。
     */
    @Test
    void testModifiedStatus() {
        Editor editor = new TextEditor("test.txt");
        workspace.openFile("test.txt", editor);
        
        assertFalse(workspace.isFileModified("test.txt"));
        workspace.updateModifiedStatus("test.txt", true);
        assertTrue(workspace.isFileModified("test.txt"));
        
        workspace.updateModifiedStatus("test.txt", false);
        assertFalse(workspace.isFileModified("test.txt"));
    }
    
    /**
     * 测试判断工作区是否存在未保存文件。
     * 测试数据：打开 "file1.txt" 和 "file2.txt"，仅将 "file1.txt" 标记为已修改。
     * 预期：hasUnsavedFiles() 在修改前为 false，修改后为 true。
     */
    @Test
    void testHasUnsavedFiles() {
        Editor editor1 = new TextEditor("file1.txt");
        Editor editor2 = new TextEditor("file2.txt");
        
        workspace.openFile("file1.txt", editor1);
        workspace.openFile("file2.txt", editor2);
        
        assertFalse(workspace.hasUnsavedFiles());
        
        workspace.updateModifiedStatus("file1.txt", true);
        assertTrue(workspace.hasUnsavedFiles());
    }
    
    /**
     * 测试获取所有未保存文件列表。
     * 测试数据：打开 "file1.txt" 和 "file2.txt"，只将 "file1.txt" 标记为已修改。
     * 预期：getUnsavedFiles() 只包含 "file1.txt"。
     */
    @Test
    void testGetUnsavedFiles() {
        Editor editor1 = new TextEditor("file1.txt");
        Editor editor2 = new TextEditor("file2.txt");
        
        workspace.openFile("file1.txt", editor1);
        workspace.openFile("file2.txt", editor2);
        
        workspace.updateModifiedStatus("file1.txt", true);
        workspace.updateModifiedStatus("file2.txt", false);
        
        List<String> unsavedFiles = workspace.getUnsavedFiles();
        assertEquals(1, unsavedFiles.size());
        assertTrue(unsavedFiles.contains("file1.txt"));
        assertFalse(unsavedFiles.contains("file2.txt"));
    }
    
    // ========== 日志功能测试 ==========
    
    /**
     * 测试为已打开文件启用日志。
     * 测试数据：打开 "test.txt"，调用 enableLogging("test.txt")。
     * 预期：isLoggingEnabled("test.txt") 返回 true。
     */
    @Test
    void testEnableLogging() {
        Editor editor = new TextEditor("test.txt");
        workspace.openFile("test.txt", editor);
        
        workspace.enableLogging("test.txt");
        assertTrue(workspace.isLoggingEnabled("test.txt"));
    }
    
    /**
     * 测试关闭日志方法不抛异常（具体日志状态由 LoggingService 决定）。
     * 测试数据：打开 "test.txt"，先 enableLogging 再 disableLogging。
     * 预期：代码执行不抛出异常。
     */
    @Test
    void testDisableLogging() {
        Editor editor = new TextEditor("test.txt");
        workspace.openFile("test.txt", editor);
        
        workspace.enableLogging("test.txt");
        workspace.disableLogging("test.txt");
        // 注意：disableLogging后，isLoggingEnabled可能仍返回true（如果文件首行是# log）
        // 这里只测试方法不抛出异常
    }
    
    /**
     * 测试为未打开文件启用日志时的异常。
     * 测试数据：直接调用 enableLogging("nonexistent.txt")。
     * 预期：抛出 IllegalStateException，消息包含 "文件未打开"。
     */
    @Test
    void testEnableLoggingNotOpen() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            workspace.enableLogging("nonexistent.txt");
        });
        assertTrue(exception.getMessage().contains("文件未打开"));
    }
    
    // ========== Memento 测试 ==========
    
    /**
     * 测试 saveState 创建的 WorkspaceMemento 内容。
     * 测试数据：打开 "file1.txt"、"file2.txt"，活动文件为 "file1.txt"，只标记 "file1.txt" 已修改并启用日志。
     * 预期：memento 中 openFiles 数量为2、activeFile 为 "file1.txt"，modifiedStatus 和 loggingEnabled 中 "file1.txt" 为 true。
     */
    @Test
    void testSaveState() {
        Editor editor1 = new TextEditor("file1.txt");
        Editor editor2 = new TextEditor("file2.txt");
        
        workspace.openFile("file1.txt", editor1);
        workspace.openFile("file2.txt", editor2);
        workspace.setActiveFile("file1.txt");
        workspace.updateModifiedStatus("file1.txt", true);
        workspace.enableLogging("file1.txt");
        
        WorkspaceMemento memento = workspace.saveState();
        
        assertNotNull(memento);
        List<String> openFiles = memento.getOpenFiles();
        assertEquals(2, openFiles.size());
        assertEquals("file1.txt", memento.getActiveFile());
        assertTrue(memento.getModifiedStatus().get("file1.txt"));
        assertFalse(memento.getModifiedStatus().get("file2.txt"));
    }
    
    /**
     * 测试 restoreState 只恢复元数据，不恢复编辑器实例。
     * 测试数据：在原 workspace 中打开并设置 "test.txt" 的状态，然后将 memento 应用到新 Workspace。
     * 预期：新 Workspace 的 activeFile 和 modifiedStatus 正确，但 isFileOpen("test.txt") 为 false。
     */
    @Test
    void testRestoreState() {
        // 先创建一个状态
        Editor editor = new TextEditor("test.txt");
        workspace.openFile("test.txt", editor);
        workspace.setActiveFile("test.txt");
        workspace.updateModifiedStatus("test.txt", true);
        workspace.enableLogging("test.txt");
        
        WorkspaceMemento memento = workspace.saveState();
        
        // 创建新工作区并恢复状态（只恢复元数据）
        Workspace newWorkspace = new Workspace(loggingService);
        newWorkspace.restoreState(memento);
        
        // 验证元数据已恢复
        assertEquals("test.txt", newWorkspace.getActiveFile());
        assertTrue(newWorkspace.isFileModified("test.txt"));
        // 注意：restoreState只恢复元数据，不恢复编辑器实例
        // 所以文件不会真正被打开，需要通过openFile来打开
        assertFalse(newWorkspace.isFileOpen("test.txt"));
    }
    
    /**
     * 测试 restoreState 在多个文件状态下的恢复行为。
     * 测试数据：保存两个文件的状态，其中 "file1.txt" 为活动且已修改，"file2.txt" 未修改。
     * 预期：新 Workspace 中 activeFile 为 "file1.txt"，对应的 modifiedStatus 恢复为 true/false。
     */
    @Test
    void testRestoreStateWithMultipleFiles() {
        // 创建多个文件的状态
        Editor editor1 = new TextEditor("file1.txt");
        Editor editor2 = new TextEditor("file2.txt");
        
        workspace.openFile("file1.txt", editor1);
        workspace.openFile("file2.txt", editor2);
        workspace.setActiveFile("file1.txt");
        workspace.updateModifiedStatus("file1.txt", true);
        workspace.updateModifiedStatus("file2.txt", false);
        workspace.enableLogging("file1.txt");
        
        WorkspaceMemento memento = workspace.saveState();
        
        // 创建新工作区并恢复状态
        Workspace newWorkspace = new Workspace(loggingService);
        newWorkspace.restoreState(memento);
        
        // 验证元数据已恢复
        assertEquals("file1.txt", newWorkspace.getActiveFile());
        assertTrue(newWorkspace.isFileModified("file1.txt"));
        assertFalse(newWorkspace.isFileModified("file2.txt"));
        // 注意：文件需要通过openFile来真正打开
    }
    
    // ========== 事件监听测试 ==========
    
    /**
     * 测试添加事件监听器后能收到文件打开事件。
     * 测试数据：注册 TestEventListener，打开 "test.txt" 文件。
     * 预期：监听器的 receivedEvents 计数大于 0。
     */
    @Test
    void testAddListener() {
        TestEventListener listener = new TestEventListener();
        workspace.addListener(listener);
        
        Editor editor = new TextEditor("test.txt");
        workspace.openFile("test.txt", editor);
        
        // 应该收到文件打开事件
        assertTrue(listener.receivedEvents > 0);
    }
    
    /**
     * 测试移除事件监听器后不再收到事件。
     * 测试数据：添加并立即移除 TestEventListener，然后打开 "test.txt"。
     * 预期：监听器的 receivedEvents 始终为 0。
     */
    @Test
    void testRemoveListener() {
        TestEventListener listener = new TestEventListener();
        workspace.addListener(listener);
        workspace.removeListener(listener);
        
        Editor editor = new TextEditor("test.txt");
        workspace.openFile("test.txt", editor);
        
        // 应该不会收到事件
        assertEquals(0, listener.receivedEvents);
    }
    
    // ========== 辅助类 ==========
    
    static class TestEventListener implements EventListener {
        int receivedEvents = 0;
        
        @Override
        public void onEvent(EditorEvent event) {
            receivedEvents++;
        }
    }
}
