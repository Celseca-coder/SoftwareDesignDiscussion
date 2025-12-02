package com.editor.core.workspace;

import com.editor.core.editor.Editor;
import com.editor.core.editor.TextEditor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

/**
 * EditorManager 单元测试
 * 测试编辑器管理器
 */
public class EditorManagerTest {
    private EditorManager editorManager;
    
    @BeforeEach
    void setUp() {
        editorManager = new EditorManager();
    }
    
    /**
     * 测试添加编辑器。
     * 测试数据：创建一个 TextEditor 实例，文件路径为 "test.txt"。
     * 预期：编辑器被成功添加到管理器中，可以通过 hasEditor 返回 true，并通过 getEditor 获取到相同的编辑器实例。
     */
    @Test
    void testAddEditor() {
        Editor editor = new TextEditor("test.txt");
        editorManager.addEditor("test.txt", editor);
        
        assertTrue(editorManager.hasEditor("test.txt"));
        assertEquals(editor, editorManager.getEditor("test.txt"));
    }
    
    /**
     * 测试获取存在的编辑器。
     * 测试数据：添加一个 TextEditor 实例，文件路径为 "test.txt"。
     * 预期：通过 getEditor 获取到的编辑器不为空，且文件路径为 "test.txt"。
     */
    @Test
    void testGetEditor() {
        Editor editor = new TextEditor("test.txt");
        editorManager.addEditor("test.txt", editor);
        
        Editor retrieved = editorManager.getEditor("test.txt");
        assertNotNull(retrieved);
        assertEquals("test.txt", retrieved.getFilePath());
    }
    
    /**
     * 测试获取不存在的编辑器。
     * 测试数据：尝试获取文件路径为 "nonexistent.txt" 的编辑器（未添加任何编辑器）。
     * 预期：getEditor 返回 null。
     */
    @Test
    void testGetEditorNotExists() {
        Editor editor = editorManager.getEditor("nonexistent.txt");
        assertNull(editor);
    }
    
    /**
     * 测试移除编辑器。
     * 测试数据：添加一个 TextEditor 实例，文件路径为 "test.txt"，然后移除它。
     * 预期：移除后，hasEditor 返回 false。
     */
    @Test
    void testRemoveEditor() {
        Editor editor = new TextEditor("test.txt");
        editorManager.addEditor("test.txt", editor);
        
        editorManager.removeEditor("test.txt");
        assertFalse(editorManager.hasEditor("test.txt"));
    }
    
    /**
     * 测试检查编辑器是否存在。
     * 测试数据：初始时检查 "test.txt"（不存在），然后添加一个 TextEditor 实例，文件路径为 "test.txt"，再次检查。
     * 预期：添加前 hasEditor 返回 false，添加后返回 true。
     */
    @Test
    void testHasEditor() {
        assertFalse(editorManager.hasEditor("test.txt"));
        
        Editor editor = new TextEditor("test.txt");
        editorManager.addEditor("test.txt", editor);
        assertTrue(editorManager.hasEditor("test.txt"));
    }
    
    /**
     * 测试获取所有文件路径。
     * 测试数据：添加两个 TextEditor 实例，文件路径分别为 "file1.txt" 和 "file2.txt"。
     * 预期：getAllFilePaths 返回的集合大小为 2，并包含 "file1.txt" 和 "file2.txt"。
     */
    @Test
    void testGetAllFilePaths() {
        Editor editor1 = new TextEditor("file1.txt");
        Editor editor2 = new TextEditor("file2.txt");
        
        editorManager.addEditor("file1.txt", editor1);
        editorManager.addEditor("file2.txt", editor2);
        
        Set<String> filePaths = editorManager.getAllFilePaths();
        assertEquals(2, filePaths.size());
        assertTrue(filePaths.contains("file1.txt"));
        assertTrue(filePaths.contains("file2.txt"));
    }
    
    /**
     * 测试获取所有编辑器。
     * 测试数据：添加两个 TextEditor 实例，文件路径分别为 "file1.txt" 和 "file2.txt"。
     * 预期：getAllEditors 返回的 Map 大小为 2，并包含正确的编辑器实例。
     */
    @Test
    void testGetAllEditors() {
        Editor editor1 = new TextEditor("file1.txt");
        Editor editor2 = new TextEditor("file2.txt");
        
        editorManager.addEditor("file1.txt", editor1);
        editorManager.addEditor("file2.txt", editor2);
        
        java.util.Map<String, Editor> editors = editorManager.getAllEditors();
        assertEquals(2, editors.size());
        assertEquals(editor1, editors.get("file1.txt"));
        assertEquals(editor2, editors.get("file2.txt"));
    }
    
    /**
     * 测试 getAllEditors 返回副本。
     * 测试数据：添加一个 TextEditor 实例，文件路径为 "test.txt"，然后获取编辑器 Map 两次，并尝试修改其中一个 Map。
     * 预期：返回的 Map 是不同的实例，修改返回的 Map 不影响管理器内部状态。
     */
    @Test
    void testGetAllEditorsReturnsCopy() {
        Editor editor = new TextEditor("test.txt");
        editorManager.addEditor("test.txt", editor);
        
        java.util.Map<String, Editor> editors1 = editorManager.getAllEditors();
        java.util.Map<String, Editor> editors2 = editorManager.getAllEditors();
        
        // 应该返回不同的副本
        assertNotSame(editors1, editors2);
        
        // 修改返回的map不应该影响管理器
        editors1.put("new.txt", new TextEditor("new.txt"));
        java.util.Map<String, Editor> editors3 = editorManager.getAllEditors();
        assertEquals(1, editors3.size());
    }
    
    /**
     * 测试多个编辑器的管理。
     * 测试数据：添加三个 TextEditor 实例，文件路径分别为 "file1.txt"、"file2.txt" 和 "file3.txt"，然后移除 "file2.txt"。
     * 预期：添加后文件路径数量为 3，移除后为 2，"file1.txt" 和 "file3.txt" 存在，"file2.txt" 不存在。
     */
    @Test
    void testMultipleEditors() {
        Editor editor1 = new TextEditor("file1.txt");
        Editor editor2 = new TextEditor("file2.txt");
        Editor editor3 = new TextEditor("file3.txt");
        
        editorManager.addEditor("file1.txt", editor1);
        editorManager.addEditor("file2.txt", editor2);
        editorManager.addEditor("file3.txt", editor3);
        
        assertEquals(3, editorManager.getAllFilePaths().size());
        
        editorManager.removeEditor("file2.txt");
        assertEquals(2, editorManager.getAllFilePaths().size());
        assertTrue(editorManager.hasEditor("file1.txt"));
        assertFalse(editorManager.hasEditor("file2.txt"));
        assertTrue(editorManager.hasEditor("file3.txt"));
    }
}
