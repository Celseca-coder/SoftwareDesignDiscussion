package editor.workspace;

import editor.core.Editor;
import editor.core.TextEditor;
import editor.observer.EventPublisher;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Workspace 工作区管理测试
 */
public class WorkspaceTest {
    private Workspace workspace;
    private String testFile1 = "test1.txt";
    private String testFile2 = "test2.txt";
    private String testFile3 = "test3.txt";

    @Before
    public void setUp() {
        workspace = new Workspace();
        deleteTestFiles();
    }

    @After
    public void tearDown() {
        deleteTestFiles();
    }

    private void deleteTestFiles() {
        try {
            Files.deleteIfExists(Paths.get(testFile1));
            Files.deleteIfExists(Paths.get(testFile2));
            Files.deleteIfExists(Paths.get(testFile3));
            Files.deleteIfExists(Paths.get(".workspace.state"));
        } catch (Exception e) {
            // 忽略删除错误
        }
    }

    // ========== 基本功能测试 ==========

    @Test
    public void testGetEventPublisher() {
        assertNotNull("EventPublisher 不应该为 null", workspace.getEventPublisher());
    }

    @Test
    public void testAddEditor() {
        TextEditor editor = new TextEditor(testFile1);
        workspace.addEditor(testFile1, editor);
        
        assertEquals("应该能获取添加的编辑器", editor, workspace.getEditor(testFile1));
        assertEquals("应该成为活动编辑器", editor, workspace.getActiveEditor());
    }

    @Test
    public void testGetEditorNotExists() {
        assertNull("不存在的编辑器应该返回 null", workspace.getEditor("non_existent.txt"));
    }

    @Test
    public void testGetActiveEditorWhenEmpty() {
        assertNull("空工作区的活动编辑器应该为 null", workspace.getActiveEditor());
    }

    @Test
    public void testSetActiveEditor() {
        TextEditor editor1 = new TextEditor(testFile1);
        TextEditor editor2 = new TextEditor(testFile2);
        
        workspace.addEditor(testFile1, editor1);
        workspace.addEditor(testFile2, editor2);
        
        workspace.setActiveEditor(editor1);
        assertEquals("应该设置为 editor1", editor1, workspace.getActiveEditor());
    }

    @Test
    public void testGetAllEditors() {
        TextEditor editor1 = new TextEditor(testFile1);
        TextEditor editor2 = new TextEditor(testFile2);
        
        workspace.addEditor(testFile1, editor1);
        workspace.addEditor(testFile2, editor2);
        
        Collection<Editor> editors = workspace.getAllEditors();
        assertEquals("应该有两个编辑器", 2, editors.size());
        assertTrue("应该包含 editor1", editors.contains(editor1));
        assertTrue("应该包含 editor2", editors.contains(editor2));
    }

    // ========== removeEditor 功能测试 ==========

    @Test
    public void testRemoveEditor() {
        TextEditor editor = new TextEditor(testFile1);
        workspace.addEditor(testFile1, editor);
        
        workspace.removeEditor(testFile1);
        assertNull("删除后应该获取不到", workspace.getEditor(testFile1));
    }

    @Test
    public void testRemoveActiveEditor() {
        TextEditor editor1 = new TextEditor(testFile1);
        TextEditor editor2 = new TextEditor(testFile2);
        
        workspace.addEditor(testFile1, editor1);
        workspace.addEditor(testFile2, editor2);
        
        workspace.removeEditor(testFile2);
        assertNotNull("应该有新的活动编辑器", workspace.getActiveEditor());
    }

    @Test
    public void testRemoveLastEditor() {
        TextEditor editor = new TextEditor(testFile1);
        workspace.addEditor(testFile1, editor);
        
        workspace.removeEditor(testFile1);
        assertNull("删除最后一个编辑器后活动编辑器应该为 null", workspace.getActiveEditor());
    }

    // ========== 最近使用文件测试 ==========

    @Test
    public void testRecentEditorsTracking() {
        TextEditor editor1 = new TextEditor(testFile1);
        TextEditor editor2 = new TextEditor(testFile2);
        TextEditor editor3 = new TextEditor(testFile3);
        
        workspace.addEditor(testFile1, editor1);
        workspace.addEditor(testFile2, editor2);
        workspace.addEditor(testFile3, editor3);
        
        // 切换到 editor1
        workspace.setActiveEditor(editor1);
        assertEquals("活动编辑器应该是 editor1", editor1, workspace.getActiveEditor());
        
        // 关闭 editor1,应该切换到 editor3 (最近使用)
        workspace.removeEditor(testFile1);
        assertEquals("应该切换到最近使用的 editor3", editor3, workspace.getActiveEditor());
    }

    @Test
    public void testRecentEditorsAfterMultipleSwitches() {
        TextEditor editor1 = new TextEditor(testFile1);
        TextEditor editor2 = new TextEditor(testFile2);
        TextEditor editor3 = new TextEditor(testFile3);
        
        workspace.addEditor(testFile1, editor1);
        workspace.addEditor(testFile2, editor2);
        workspace.addEditor(testFile3, editor3);
        
        // 切换顺序: 3 -> 1 -> 2
        workspace.setActiveEditor(editor1);
        workspace.setActiveEditor(editor2);
        
        // 关闭 editor2,应该切换到 editor1
        workspace.removeEditor(testFile2);
        assertEquals("应该切换到 editor1", editor1, workspace.getActiveEditor());
        
        // 再关闭 editor1,应该切换到 editor3
        workspace.removeEditor(testFile1);
        assertEquals("应该切换到 editor3", editor3, workspace.getActiveEditor());
    }

    // ========== 工作区状态持久化测试 ==========

    @Test
    public void testSaveWorkspaceState() {
        TextEditor editor1 = new TextEditor(testFile1);
        workspace.addEditor(testFile1, editor1);
        
        workspace.saveWorkspaceState();
        assertTrue("状态文件应该存在", new File(".workspace.state").exists());
    }

    @Test
    public void testSaveEmptyWorkspace() {
        workspace.saveWorkspaceState();
        assertTrue("即使工作区为空也应该保存状态", new File(".workspace.state").exists());
    }
}
