package com.editor.integration;

import com.editor.core.editor.Editor;
import com.editor.core.editor.TextEditor;
import com.editor.core.logging.LoggingService;
import com.editor.core.persistence.FilePersistence;
import com.editor.core.persistence.WorkspacePersistence;
import com.editor.core.workspace.Workspace;
import com.editor.core.workspace.WorkspaceMemento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * 工作区恢复集成测试
 * 测试完整的文件恢复流程（模拟 Main.java 中的恢复逻辑）
 */
public class WorkspaceRestoreIntegrationTest {
    @TempDir
    Path tempDir;
    
    private FilePersistence filePersistence;
    private WorkspacePersistence workspacePersistence;
    private LoggingService loggingService;
    private Path originalDir;
    
    @BeforeEach
    void setUp() {
        filePersistence = new FilePersistence();
        workspacePersistence = new WorkspacePersistence();
        loggingService = new LoggingService(filePersistence);
        originalDir = Path.of(System.getProperty("user.dir"));
    }
    
    /**
     * 测试完整的文件恢复流程
     * 模拟 Main.java 中的恢复逻辑
     */
    @Test
    void testFullFileRestore() throws IOException {
        // 切换到临时目录
        System.setProperty("user.dir", tempDir.toString());
        
        try {
            // 1. 创建测试文件
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Files.write(file1, Arrays.asList("Line 1", "Line 2"), StandardCharsets.UTF_8);
            Files.write(file2, Arrays.asList("Content"), StandardCharsets.UTF_8);
            
            // 2. 创建初始工作区并打开文件
            Workspace originalWorkspace = new Workspace(loggingService);
            
            // 加载文件内容到编辑器
            List<String> file1Lines = filePersistence.loadFile(file1.toString());
            Editor editor1 = new TextEditor(file1.toString(), file1Lines);
            editor1.append("Line 3"); // 添加第3行
            editor1.setModified(true);
            
            List<String> file2Lines = filePersistence.loadFile(file2.toString());
            Editor editor2 = new TextEditor(file2.toString(), file2Lines);
            
            originalWorkspace.openFile(file1.toString(), editor1);
            originalWorkspace.openFile(file2.toString(), editor2);
            originalWorkspace.setActiveFile(file1.toString());
            originalWorkspace.enableLogging(file1.toString());
            
            // 3. 保存文件内容（确保文件系统中的内容与编辑器一致）
            // 注意：在实际场景中，如果文件被修改但未保存，恢复时会从文件系统加载旧内容
            // 这里我们先保存文件，以测试保存后的恢复场景
            filePersistence.saveFile(file1.toString(), editor1.show());
            editor1.setModified(false);
            originalWorkspace.updateModifiedStatus(file1.toString(), false);
            
            // 4. 保存工作区状态
            WorkspaceMemento memento = originalWorkspace.saveState();
            workspacePersistence.save(memento);
            
            // 5. 创建新工作区并恢复状态（模拟 Main.java 的恢复逻辑）
            Workspace restoredWorkspace = new Workspace(loggingService);
            WorkspaceMemento loadedMemento = workspacePersistence.load();
            restoredWorkspace.restoreState(loadedMemento);
            
            // 6. 恢复打开的文件
            List<String> openFiles = loadedMemento.getOpenFiles();
            String activeFile = loadedMemento.getActiveFile();
            
            for (String filePath : openFiles) {
                // 加载文件内容
                List<String> lines = filePersistence.loadFile(filePath);
                
                // 创建编辑器实例
                Editor editor = new TextEditor(filePath, lines);
                
                // 恢复修改状态
                boolean wasModified = loadedMemento.getModifiedStatus().getOrDefault(filePath, false);
                if (wasModified) {
                    editor.setModified(true);
                }
                
                // 打开文件
                restoredWorkspace.openFile(filePath, editor);
                
                // 恢复日志状态
                if (loadedMemento.getLoggingEnabled().getOrDefault(filePath, false)) {
                    restoredWorkspace.enableLogging(filePath);
                }
            }
            
            // 恢复活动文件
            if (activeFile != null && restoredWorkspace.isFileOpen(activeFile)) {
                restoredWorkspace.setActiveFile(activeFile);
            }
            
            // 7. 验证恢复结果
            assertTrue(restoredWorkspace.isFileOpen(file1.toString()));
            assertTrue(restoredWorkspace.isFileOpen(file2.toString()));
            assertEquals(file1.toString(), restoredWorkspace.getActiveFile());
            // 注意：由于我们在恢复前保存了文件，所以修改状态应该是false
            assertFalse(restoredWorkspace.isFileModified(file1.toString()));
            assertFalse(restoredWorkspace.isFileModified(file2.toString()));
            assertTrue(restoredWorkspace.isLoggingEnabled(file1.toString()));
            
            // 验证文件内容已恢复（从文件系统加载，应该包含保存的内容）
            Editor restoredEditor1 = restoredWorkspace.getEditor(file1.toString());
            List<String> content1 = restoredEditor1.show();
            assertEquals(3, content1.size());
            assertEquals("Line 1", content1.get(0));
            assertEquals("Line 2", content1.get(1));
            assertEquals("Line 3", content1.get(2));
            
            Editor restoredEditor2 = restoredWorkspace.getEditor(file2.toString());
            List<String> content2 = restoredEditor2.show();
            assertEquals(1, content2.size());
            assertEquals("Content", content2.get(0));
            
        } finally {
            System.setProperty("user.dir", originalDir.toString());
        }
    }
    
    /**
     * 测试未保存文件的恢复场景
     * 当文件被修改但未保存时，恢复应该从文件系统加载旧内容
     */
    @Test
    void testRestoreUnsavedFile() throws IOException {
        System.setProperty("user.dir", tempDir.toString());
        
        try {
            // 1. 创建文件并写入初始内容
            Path testFile = tempDir.resolve("unsaved.txt");
            Files.write(testFile, Arrays.asList("Original Line 1", "Original Line 2"), StandardCharsets.UTF_8);
            
            // 2. 打开文件并修改（但不保存）
            Workspace originalWorkspace = new Workspace(loggingService);
            Editor editor = new TextEditor(testFile.toString());
            editor.append("New Line 3");
            editor.setModified(true); // 标记为已修改但未保存
            
            originalWorkspace.openFile(testFile.toString(), editor);
            originalWorkspace.setActiveFile(testFile.toString());
            
            // 3. 保存工作区状态（但不保存文件内容）
            WorkspaceMemento memento = originalWorkspace.saveState();
            workspacePersistence.save(memento);
            
            // 4. 恢复工作区
            Workspace restoredWorkspace = new Workspace(loggingService);
            WorkspaceMemento loadedMemento = workspacePersistence.load();
            restoredWorkspace.restoreState(loadedMemento);
            
            // 5. 恢复文件（从文件系统加载，应该是旧内容）
            for (String filePath : loadedMemento.getOpenFiles()) {
                if (!filePersistence.fileExists(filePath)) {
                    continue;
                }
                List<String> lines = filePersistence.loadFile(filePath);
                Editor restoredEditor = new TextEditor(filePath, lines);
                boolean wasModified = loadedMemento.getModifiedStatus().getOrDefault(filePath, false);
                if (wasModified) {
                    restoredEditor.setModified(true);
                }
                restoredWorkspace.openFile(filePath, restoredEditor);
            }
            
            // 恢复活动文件
            String activeFile = loadedMemento.getActiveFile();
            if (activeFile != null && restoredWorkspace.isFileOpen(activeFile)) {
                restoredWorkspace.setActiveFile(activeFile);
            }
            
            // 6. 验证：恢复后的内容应该是文件系统中的旧内容（2行），而不是编辑器中的新内容（3行）
            Editor restoredEditor = restoredWorkspace.getEditor(testFile.toString());
            List<String> content = restoredEditor.show();
            assertEquals(2, content.size(), "未保存的修改不应该在恢复时保留");
            assertEquals("Original Line 1", content.get(0));
            assertEquals("Original Line 2", content.get(1));
            // 注意：虽然文件内容恢复到旧版本，但修改状态应该被恢复
            assertTrue(restoredWorkspace.isFileModified(testFile.toString()), 
                      "修改状态应该被恢复，即使内容从文件系统加载");
            
        } finally {
            System.setProperty("user.dir", originalDir.toString());
        }
    }
    
    /**
     * 测试恢复时文件不存在的情况
     */
    @Test
    void testRestoreWithMissingFile() throws IOException {
        System.setProperty("user.dir", tempDir.toString());
        
        try {
            // 1. 创建一个文件并保存状态
            Path existingFile = tempDir.resolve("existing.txt");
            Files.write(existingFile, Arrays.asList("Content"), StandardCharsets.UTF_8);
            
            Workspace originalWorkspace = new Workspace(loggingService);
            Editor editor = new TextEditor(existingFile.toString());
            originalWorkspace.openFile(existingFile.toString(), editor);
            originalWorkspace.setActiveFile(existingFile.toString());
            
            WorkspaceMemento memento = originalWorkspace.saveState();
            workspacePersistence.save(memento);
            
            // 2. 删除文件
            Files.delete(existingFile);
            
            // 3. 尝试恢复（应该跳过不存在的文件）
            Workspace restoredWorkspace = new Workspace(loggingService);
            WorkspaceMemento loadedMemento = workspacePersistence.load();
            restoredWorkspace.restoreState(loadedMemento);
            
            List<String> openFiles = loadedMemento.getOpenFiles();
            for (String filePath : openFiles) {
                // 检查文件是否存在（FilePersistence.loadFile 在文件不存在时返回空列表，不抛异常）
                if (!filePersistence.fileExists(filePath)) {
                    // 文件不存在，跳过该文件
                    continue;
                }
                
                try {
                    List<String> lines = filePersistence.loadFile(filePath);
                    Editor restoredEditor = new TextEditor(filePath, lines);
                    restoredWorkspace.openFile(filePath, restoredEditor);
                } catch (IOException e) {
                    // 如果加载失败，跳过该文件
                }
            }
            
            // 验证文件没有被打开（因为文件不存在）
            assertFalse(restoredWorkspace.isFileOpen(existingFile.toString()));
            
        } finally {
            System.setProperty("user.dir", originalDir.toString());
        }
    }
    
    /**
     * 测试恢复多个文件时的活动文件恢复
     */
    @Test
    void testRestoreActiveFile() throws IOException {
        System.setProperty("user.dir", tempDir.toString());
        
        try {
            // 创建多个文件
            Path file1 = tempDir.resolve("file1.txt");
            Path file2 = tempDir.resolve("file2.txt");
            Path file3 = tempDir.resolve("file3.txt");
            Files.write(file1, Arrays.asList("File 1"), StandardCharsets.UTF_8);
            Files.write(file2, Arrays.asList("File 2"), StandardCharsets.UTF_8);
            Files.write(file3, Arrays.asList("File 3"), StandardCharsets.UTF_8);
            
            // 创建初始工作区
            Workspace originalWorkspace = new Workspace(loggingService);
            originalWorkspace.openFile(file1.toString(), new TextEditor(file1.toString()));
            originalWorkspace.openFile(file2.toString(), new TextEditor(file2.toString()));
            originalWorkspace.openFile(file3.toString(), new TextEditor(file3.toString()));
            originalWorkspace.setActiveFile(file2.toString()); // 设置 file2 为活动文件
            
            // 保存状态
            WorkspaceMemento memento = originalWorkspace.saveState();
            workspacePersistence.save(memento);
            
            // 恢复工作区
            Workspace restoredWorkspace = new Workspace(loggingService);
            WorkspaceMemento loadedMemento = workspacePersistence.load();
            restoredWorkspace.restoreState(loadedMemento);
            
            // 恢复文件
            for (String filePath : loadedMemento.getOpenFiles()) {
                List<String> lines = filePersistence.loadFile(filePath);
                Editor editor = new TextEditor(filePath, lines);
                restoredWorkspace.openFile(filePath, editor);
            }
            
            // 恢复活动文件
            String activeFile = loadedMemento.getActiveFile();
            if (activeFile != null && restoredWorkspace.isFileOpen(activeFile)) {
                restoredWorkspace.setActiveFile(activeFile);
            }
            
            // 验证活动文件已恢复
            assertEquals(file2.toString(), restoredWorkspace.getActiveFile());
            
        } finally {
            System.setProperty("user.dir", originalDir.toString());
        }
    }
}

