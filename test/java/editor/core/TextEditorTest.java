package editor.core;

import editor.observer.EventPublisher;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * TextEditor 核心功能测试
 */
public class TextEditorTest {
    private TextEditor editor;
    private String testFilePath = "test_file.txt";
    private EventPublisher eventPublisher;

    @Before
    public void setUp() {
        editor = new TextEditor(testFilePath);
        eventPublisher = new EventPublisher();
        // 清理测试文件
        deleteTestFiles();
    }

    @After
    public void tearDown() {
        deleteTestFiles();
    }

    private void deleteTestFiles() {
        try {
            Files.deleteIfExists(Paths.get(testFilePath));
            Files.deleteIfExists(Paths.get("." + testFilePath + ".log"));
        } catch (Exception e) {
            // 忽略删除错误
        }
    }

    // ========== 基本属性测试 ==========

    @Test
    public void testGetFilePath() {
        assertEquals(testFilePath, editor.getFilePath());
    }

    @Test
    public void testInitialModifiedState() {
        assertFalse("新建编辑器应该未修改", editor.isModified());
    }

    @Test
    public void testSetModified() {
        editor.setModified(true);
        assertTrue("设置修改后应该为已修改", editor.isModified());
    }

    // ========== append 功能测试 ==========

    @Test
    public void testAppendSingleLine() {
        editor.append("第一行");
        List<String> lines = editor.getLines();
        assertEquals("应该有一行", 1, lines.size());
        assertEquals("内容应该匹配", "第一行", lines.get(0));
    }

    @Test
    public void testAppendMultipleLines() {
        editor.append("第一行");
        editor.append("第二行");
        editor.append("第三行");
        List<String> lines = editor.getLines();
        assertEquals("应该有三行", 3, lines.size());
        assertEquals("第一行内容", "第一行", lines.get(0));
        assertEquals("第二行内容", "第二行", lines.get(1));
        assertEquals("第三行内容", "第三行", lines.get(2));
    }

    // ========== insert 功能测试 ==========

    @Test
    public void testInsertInEmptyFile() {
        editor.insert(1, 1, "内容");
        List<String> lines = editor.getLines();
        assertEquals("应该有一行", 1, lines.size());
        assertEquals("内容应该匹配", "内容", lines.get(0));
    }

    @Test
    public void testInsertAtBeginning() {
        editor.append("world");
        editor.insert(1, 1, "Hello ");
        assertEquals("应该是 'Hello world'", "Hello world", editor.getLines().get(0));
    }

    @Test
    public void testInsertAtMiddle() {
        editor.append("abcdef");
        editor.insert(1, 4, "XYZ");
        assertEquals("应该在中间插入", "abcXYZdef", editor.getLines().get(0));
    }

    @Test
    public void testInsertAtEnd() {
        editor.append("Hello");
        editor.insert(1, 6, " World");
        assertEquals("应该在末尾插入", "Hello World", editor.getLines().get(0));
    }

    @Test
    public void testInsertMultipleLines() {
        editor.append("第一行");
        editor.insert(1, 4, "\n新行\n");
        List<String> lines = editor.getLines();
        assertTrue("应该有多行", lines.size() > 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertInvalidLine() {
        editor.append("test");
        editor.insert(5, 1, "error");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertInvalidColumn() {
        editor.append("test");
        editor.insert(1, 10, "error");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertInEmptyFileWrongPosition() {
        editor.insert(1, 2, "error");
    }

    // ========== delete 功能测试 ==========

    @Test
    public void testDeletePartialLine() {
        editor.append("Hello world");
        editor.delete(1, 7, 5);
        assertEquals("应该删除 'world'", "Hello ", editor.getLines().get(0));
    }

    @Test
    public void testDeleteFromBeginning() {
        editor.append("Hello world");
        editor.delete(1, 1, 6);
        assertEquals("应该删除 'Hello '", "world", editor.getLines().get(0));
    }

    @Test
    public void testDeleteToEnd() {
        editor.append("Hello world");
        editor.delete(1, 7, 5);
        assertEquals("应该删除到末尾", "Hello ", editor.getLines().get(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteBeyondLineEnd() {
        editor.append("Hello");
        editor.delete(1, 1, 10);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteInvalidLine() {
        editor.append("test");
        editor.delete(5, 1, 1);
    }

    // ========== getDeletedText 功能测试 ==========

    @Test
    public void testGetDeletedText() {
        editor.append("Hello world");
        String deleted = editor.getDeletedText(1, 7, 5);
        assertEquals("应该获取被删除的文本", "world", deleted);
    }

    // ========== save 和 load 功能测试 ==========

    @Test
    public void testSaveAndLoad() throws Exception {
        editor.append("第一行");
        editor.append("第二行");
        editor.save();
        
        assertTrue("文件应该存在", new File(testFilePath).exists());
        assertFalse("保存后应该未修改", editor.isModified());
        
        TextEditor newEditor = new TextEditor(testFilePath);
        newEditor.load();
        List<String> lines = newEditor.getLines();
        assertEquals("应该有两行", 2, lines.size());
        assertEquals("第一行内容", "第一行", lines.get(0));
        assertEquals("第二行内容", "第二行", lines.get(1));
    }

    @Test
    public void testLoadNonExistentFile() throws Exception {
        TextEditor newEditor = new TextEditor("non_existent.txt");
        newEditor.load();
        assertTrue("文件不存在时应该创建空编辑器", newEditor.getLines().isEmpty());
        assertTrue("应该标记为已修改", newEditor.isModified());
    }

    // ========== 日志功能测试 ==========

    @Test
    public void testEnableLogging() {
        assertFalse("初始状态日志应该未启用", editor.isLoggingEnabled());
        editor.enableLogging(eventPublisher);
        assertTrue("启用后日志应该已启用", editor.isLoggingEnabled());
    }

    @Test
    public void testDisableLogging() {
        editor.enableLogging(eventPublisher);
        assertTrue("日志应该已启用", editor.isLoggingEnabled());
        editor.disableLogging(eventPublisher);
        assertFalse("关闭后日志应该未启用", editor.isLoggingEnabled());
    }

    // ========== show 功能测试 ==========

    @Test
    public void testShow() {
        editor.append("第一行");
        editor.append("第二行");
        editor.show(); // 应该不抛出异常
    }

    @Test
    public void testShowRange() {
        editor.append("第一行");
        editor.append("第二行");
        editor.append("第三行");
        editor.show(1, 2); // 应该不抛出异常
    }

    // ========== undo/redo 基本功能测试 ==========

    @Test
    public void testUndoOnEmptyStack() {
        editor.undo(); // 应该不抛出异常,只是提示
    }

    @Test
    public void testRedoOnEmptyStack() {
        editor.redo(); // 应该不抛出异常,只是提示
    }
}
