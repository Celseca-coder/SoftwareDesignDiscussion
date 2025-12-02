package com.editor.core.persistence;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WorkspacePersistence 单元测试
 * 测试工作区持久化功能（保存和加载工作区状态）
 */
public class WorkspacePersistenceTest {
    @TempDir
    Path tempDir;
    
    private WorkspacePersistence persistence;
    
    @BeforeEach
    void setUp() {
        persistence = new WorkspacePersistence(getWorkspaceFile());
    }
    
    /**
     * 测试保存和加载完整工作区状态。
     * 测试数据：openFiles ["file1.txt", "file2.txt"]，activeFile "file1.txt"，modifiedStatus 和 loggingEnabled 有对应值。
     * 预期：保存到临时目录的 .editor_workspace 文件，加载后数据完全匹配。
     */
    @Test
    void testSaveAndLoadWorkspaceMemento() throws IOException {
        // 创建测试数据
        List<String> openFiles = Arrays.asList("file1.txt", "file2.txt");
        String activeFile = "file1.txt";
        Map<String, Boolean> modifiedStatus = new HashMap<>();
        modifiedStatus.put("file1.txt", true);
        modifiedStatus.put("file2.txt", false);
        Map<String, Boolean> loggingEnabled = new HashMap<>();
        loggingEnabled.put("file1.txt", true);
        loggingEnabled.put("file2.txt", false);
        
        WorkspaceMemento original = new WorkspaceMemento(
            openFiles, activeFile, modifiedStatus, loggingEnabled
        );
        
        // 保存并加载
        persistence.save(original);
        
        WorkspaceMemento loaded = persistence.load();
        
        assertNotNull(loaded);
        List<String> loadedFiles = loaded.getOpenFiles();
        assertEquals(2, loadedFiles.size());
        assertTrue(loadedFiles.contains("file1.txt"));
        assertTrue(loadedFiles.contains("file2.txt"));
        assertEquals("file1.txt", loaded.getActiveFile());
        
        Map<String, Boolean> loadedModified = loaded.getModifiedStatus();
        assertTrue(loadedModified.get("file1.txt"));
        assertFalse(loadedModified.get("file2.txt"));
        
        Map<String, Boolean> loadedLogging = loaded.getLoggingEnabled();
        assertTrue(loadedLogging.get("file1.txt"));
        assertFalse(loadedLogging.get("file2.txt"));
    }
    
    /**
     * 测试加载不存在的工作区文件。
     * 测试数据：临时目录，无 .editor_workspace 文件。
     * 预期：返回空的 WorkspaceMemento，所有字段为空或默认值。
     */
    @Test
    void testLoadNonExistentWorkspace() throws IOException {
        // 加载不存在的文件应该返回空状态
        WorkspaceMemento loaded = persistence.load();
        
        assertNotNull(loaded);
        assertTrue(loaded.getOpenFiles().isEmpty());
        assertNull(loaded.getActiveFile());
        assertTrue(loaded.getModifiedStatus().isEmpty());
        assertTrue(loaded.getLoggingEnabled().isEmpty());
    }
    
    /**
     * 测试保存和加载空工作区。
     * 测试数据：空的 openFiles, null activeFile, 空 maps。
     * 预期：保存后加载，结果与原数据一致。
     */
    @Test
    void testSaveAndLoadEmptyWorkspace() throws IOException {
        WorkspaceMemento empty = new WorkspaceMemento(
            Arrays.asList(), null, new HashMap<>(), new HashMap<>()
        );
        
        persistence.save(empty);
        
        WorkspaceMemento loaded = persistence.load();
        
        assertNotNull(loaded);
        assertTrue(loaded.getOpenFiles().isEmpty());
        assertNull(loaded.getActiveFile());
        assertTrue(loaded.getModifiedStatus().isEmpty());
        assertTrue(loaded.getLoggingEnabled().isEmpty());
    }
    
    /**
     * 测试保存和加载 null 活动文件的工作区。
     * 测试数据：openFiles 有两个文件, activeFile 为 null。
     * 预期：保存后加载，openFiles 正确，activeFile 为 null。
     */
    @Test
    void testSaveAndLoadWithNullActiveFile() throws IOException {
        List<String> openFiles = Arrays.asList("file1.txt", "file2.txt");
        WorkspaceMemento memento = new WorkspaceMemento(
            openFiles, null, new HashMap<>(), new HashMap<>()
        );
        
        persistence.save(memento);
        
        WorkspaceMemento loaded = persistence.load();
        
        assertNotNull(loaded);
        assertEquals(2, loaded.getOpenFiles().size());
        assertNull(loaded.getActiveFile());
    }
    
    /**
     * 测试保存时写入预期的区段结构。
     * 测试数据：openFiles ["doc.txt"]，activeFile "doc.txt"，modifiedStatus {"doc.txt"=true}，loggingEnabled {"doc.txt"=true}。
     * 预期：.editor_workspace 文件包含正确的区段（# openFiles, # activeFile, # modifiedStatus, # loggingEnabled），顺序正确，内容包含 "doc.txt" 和 "doc.txt=true"。
     */
    @Test
    void testSaveWritesExpectedSections() throws IOException {
        Map<String, Boolean> modified = new HashMap<>();
        modified.put("doc.txt", true);
        Map<String, Boolean> logging = new HashMap<>();
        logging.put("doc.txt", true);
        
        WorkspaceMemento memento = new WorkspaceMemento(
            Arrays.asList("doc.txt"),
            "doc.txt",
            modified,
            logging
        );
        
        persistence.save(memento);
        
        Path file = getWorkspaceFile();
        assertTrue(Files.exists(file), "Workspace file should be created");
        
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int openStart = lines.indexOf("# openFiles");
        int openEnd = lines.indexOf("# endOpenFiles");
        int activeStart = lines.indexOf("# activeFile");
        int activeEnd = lines.indexOf("# endActiveFile");
        int modifiedStart = lines.indexOf("# modifiedStatus");
        int modifiedEnd = lines.indexOf("# endModifiedStatus");
        int loggingStart = lines.indexOf("# loggingEnabled");
        int loggingEnd = lines.indexOf("# endLoggingEnabled");
        
        assertTrue(openStart >= 0 && openEnd > openStart, "openFiles section 缺失");
        assertTrue(activeStart >= 0 && activeEnd > activeStart, "activeFile section 缺失");
        assertTrue(modifiedStart >= 0 && modifiedEnd > modifiedStart, "modifiedStatus section 缺失");
        assertTrue(loggingStart >= 0 && loggingEnd > loggingStart, "loggingEnabled section 缺失");
        assertTrue(openEnd < activeStart && activeEnd < modifiedStart && modifiedEnd < loggingStart,
                "区段顺序不符合实现");
        assertTrue(lines.contains("doc.txt"));
        assertTrue(lines.contains("doc.txt=true"));
    }
    
    /**
     * 测试加载时忽略未知行。
     * 测试数据：包含未知头、意外文本、无效行和未知区段的文件内容。
     * 预期：正确解析有效区段，忽略无效内容，openFiles=["fileA.txt"]，activeFile="fileA.txt"，modifiedStatus={"fileA.txt"=true}，loggingEnabled={"fileA.txt"=false}。
     */
    @Test
    void testLoadIgnoresUnknownLines() throws IOException {
        List<String> content = Arrays.asList(
            "unknown header",
            "# openFiles",
            "fileA.txt",
            "# endOpenFiles",
            "unexpected trailing text",
            "# activeFile",
            "fileA.txt",
            "# endActiveFile",
            "# modifiedStatus",
            "fileA.txt=true",
            "invalidLineWithoutEquals",
            "# endModifiedStatus",
            "# loggingEnabled",
            "fileA.txt=false",
            "# endLoggingEnabled",
            "# unknownSection",
            "???"
        );
        Files.write(getWorkspaceFile(), content, StandardCharsets.UTF_8);
        
        WorkspaceMemento loaded = persistence.load();
        assertEquals(1, loaded.getOpenFiles().size());
        assertEquals("fileA.txt", loaded.getOpenFiles().get(0));
        assertEquals("fileA.txt", loaded.getActiveFile());
        assertTrue(loaded.getModifiedStatus().getOrDefault("fileA.txt", false));
        assertFalse(loaded.getLoggingEnabled().getOrDefault("fileA.txt", true));
    }
    
    private Path getWorkspaceFile() {
        return tempDir.resolve(".editor_workspace");
    }
}

