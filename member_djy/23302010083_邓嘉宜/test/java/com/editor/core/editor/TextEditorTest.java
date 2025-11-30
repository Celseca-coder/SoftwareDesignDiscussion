package com.editor.core.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

/**
 * TextEditor 单元测试
 * 测试编辑器的所有基本操作
 */
public class TextEditorTest {
    private TextEditor editor;
    
    @BeforeEach
    void setUp() {
        editor = new TextEditor("test.txt");
    }
    
    // ========== Append 测试 ==========
    
    /**
     * 测试 append 单行文本。
     * 测试数据：追加 "Line 1"。
     * 预期：行数为1，第一行为 "Line 1"，修改状态为 true。
     */
    @Test
    void testAppend() {
        editor.append("Line 1");
        List<String> lines = editor.show();
        assertEquals(1, lines.size());
        assertEquals("Line 1", lines.get(0));
        assertTrue(editor.isModified());
    }
    
    /**
     * 测试 append 多行文本。
     * 测试数据：依次追加 "Line 1"、"Line 2"、"Line 3"。
     * 预期：行数为3，内容依次为 "Line 1"、"Line 2"、"Line 3"。
     */
    @Test
    void testAppendMultiple() {
        editor.append("Line 1");
        editor.append("Line 2");
        editor.append("Line 3");
        List<String> lines = editor.show();
        assertEquals(3, lines.size());
        assertEquals("Line 1", lines.get(0));
        assertEquals("Line 2", lines.get(1));
        assertEquals("Line 3", lines.get(2));
    }
    
    /**
     * 测试 append 空字符串。
     * 测试数据：追加 ""。
     * 预期：行数为1，第一行为空字符串。
     */
    @Test
    void testAppendEmptyString() {
        editor.append("");
        List<String> lines = editor.show();
        assertEquals(1, lines.size());
        assertEquals("", lines.get(0));
    }
    
    /**
     * 测试 append 含换行文本。
     * 测试数据：追加 "Line 1\nLine 2"。
     * 预期：自动拆分为两行。
     */
    @Test
    void testAppendWithNewline() {
        editor.append("Line 1\nLine 2");
        List<String> lines = editor.show();
        assertEquals(2, lines.size());
        assertEquals("Line 1", lines.get(0));
        assertEquals("Line 2", lines.get(1));
    }
    
    // ========== Insert 测试 ==========
    
    /**
     * 测试 insert 在行中插入文本。
     * 测试数据：先追加 "Hello World"，然后在第1行第6列插入 ", "。
     * 预期：结果为 "Hello,  World"，修改状态为 true。
     */
    @Test
    void testInsert() throws EditorException {
        editor.append("Hello World");
        editor.insert(1, 6, ", ");
        List<String> lines = editor.show();
        assertEquals("Hello,  World", lines.get(0));
        assertTrue(editor.isModified());
    }
    
    /**
     * 测试 insert 在行首插入。
     * 测试数据：先追加 "World"，然后在第1行第1列插入 "Hello "。
     * 预期：结果为 "Hello World"。
     */
    @Test
    void testInsertAtBeginning() throws EditorException {
        editor.append("World");
        editor.insert(1, 1, "Hello ");
        List<String> lines = editor.show();
        assertEquals("Hello World", lines.get(0));
    }
    
    /**
     * 测试 insert 在行尾插入。
     * 测试数据：先追加 "Hello"，然后在第1行第6列插入 " World"。
     * 预期：结果为 "Hello World"。
     */
    @Test
    void testInsertAtEnd() throws EditorException {
        editor.append("Hello");
        editor.insert(1, 6, " World");
        List<String> lines = editor.show();
        assertEquals("Hello World", lines.get(0));
    }
    
    /**
     * 测试 insert 包含换行符的文本。
     * 测试数据：先追加 "Line 1"，然后在第1行第7列插入 "\nLine 2\nLine 3"。
     * 预期：行数为3，内容分别为 "Line 1"、"Line 2"、"Line 3"。
     */
    @Test
    void testInsertWithNewline() throws EditorException {
        editor.append("Line 1");
        editor.insert(1, 7, "\nLine 2\nLine 3");
        List<String> lines = editor.show();
        assertEquals(3, lines.size());
        assertEquals("Line 1", lines.get(0));   
        assertEquals("Line 2", lines.get(1));   
        assertEquals("Line 3", lines.get(2));   
    }
    
    /**
     * 测试在空文件中 insert。
     * 测试数据：在空编辑器第1行第1列插入 "First line"。
     * 预期：行数为1，内容为 "First line"。
     */
    @Test
    void testInsertEmptyFile() throws EditorException {
        // 空文件只能在1:1位置插入
        editor.insert(1, 1, "First line");
        List<String> lines = editor.show();
        assertEquals(1, lines.size());
        assertEquals("First line", lines.get(0));
    }
    
    /**
     * 测试在空文件中 insert 无效位置。
     * 测试数据：在空编辑器第1行第2列插入 "text"。
     * 预期：抛出 EditorException，消息包含 "空文件只能在1:1位置插入"。
     */
    @Test
    void testInsertEmptyFileInvalidPosition() {
        // 空文件不能在非1:1位置插入
        EditorException exception = assertThrows(EditorException.class, () -> {
            editor.insert(1, 2, "text");
        });
        assertTrue(exception.getMessage().contains("空文件只能在1:1位置插入"));
    }
    
    /**
     * 测试 insert 行号越界。
     * 测试数据：追加 "Hello"，然后在第2行第1列插入 "text"。
     * 预期：抛出 EditorException，消息包含 "行号越界"。
     */
    @Test
    void testInsertOutOfBounds() {
        editor.append("Hello");
        EditorException exception = assertThrows(EditorException.class, () -> {
            editor.insert(2, 1, "text"); // 行号越界
        });
        assertTrue(exception.getMessage().contains("行号越界"));
    }
    
    /**
     * 测试 insert 列号越界。
     * 测试数据：追加 "Hello"，然后在第1行第100列插入 "text"。
     * 预期：抛出 EditorException，消息包含 "列号越界"。
     */
    @Test
    void testInsertColumnOutOfBounds() {
        editor.append("Hello");
        EditorException exception = assertThrows(EditorException.class, () -> {
            editor.insert(1, 100, "text"); // 列号越界
        });
        assertTrue(exception.getMessage().contains("列号越界"));
    }
    
    // ========== Delete 测试 ==========
    
    /**
     * 测试 delete 删除部分文本。
     * 测试数据：追加 "Hello World"，然后删除第1行第7列开始5个字符。
     * 预期：结果为 "Hello "，修改状态为 true。
     */
    @Test
    void testDelete() throws EditorException {
        editor.append("Hello World");
        editor.delete(1, 7, 5);
        List<String> lines = editor.show();
        assertEquals("Hello ", lines.get(0));
        assertTrue(editor.isModified());
    }
    
    /**
     * 测试 delete 从行首删除。
     * 测试数据：追加 "Hello World"，然后删除第1行第1列开始6个字符。
     * 预期：结果为 "World"。
     */
    @Test
    void testDeleteFromBeginning() throws EditorException {
        editor.append("Hello World");
        editor.delete(1, 1, 6);
        List<String> lines = editor.show();
        assertEquals("World", lines.get(0));
    }
    
    /**
     * 测试 delete 从行尾删除。
     * 测试数据：追加 "Hello World"，然后删除第1行第7列开始5个字符。
     * 预期：结果为 "Hello "。
     */
    @Test
    void testDeleteAtEnd() throws EditorException {
        editor.append("Hello World");
        editor.delete(1, 7, 5);
        List<String> lines = editor.show();
        assertEquals("Hello ", lines.get(0));
    }
    
    /**
     * 测试 delete 删除整行。
     * 测试数据：追加 "Line 1"和"Line 2"，然后删除第1行第1列开始6个字符。
     * 预期：第1行为 ""，第2行为 "Line 2"。
     */
    @Test
    void testDeleteEntireLine() throws EditorException {
        editor.append("Line 1");
        editor.append("Line 2");
        editor.delete(1, 1, 6);
        List<String> lines = editor.show();
        assertEquals("", lines.get(0));
        assertEquals("Line 2", lines.get(1));
    }
    
    /**
     * 测试 delete 删除长度超出。
     * 测试数据：追加 "Hello"，然后删除第1行第3列开始10个字符。
     * 预期：抛出 EditorException，消息包含 "删除长度超出行尾"。
     */
    @Test
    void testDeleteExceedsLine() {
        editor.append("Hello");
        EditorException exception = assertThrows(EditorException.class, () -> {
            editor.delete(1, 3, 10); // 删除长度超出
        });
        assertTrue(exception.getMessage().contains("删除长度超出行尾"));
    }
    
    /**
     * 测试 delete 行号越界。
     * 测试数据：追加 "Hello"，然后删除第2行第1列开始1个字符。
     * 预期：抛出 EditorException，消息包含 "行号越界"。
     */
    @Test
    void testDeleteOutOfBounds() {
        editor.append("Hello");
        EditorException exception = assertThrows(EditorException.class, () -> {
            editor.delete(2, 1, 1); // 行号越界
        });
        assertTrue(exception.getMessage().contains("行号越界"));
    }
    
    /**
     * 测试 delete 列号越界。
     * 测试数据：追加 "Hello"，然后删除第1行第10列开始1个字符。
     * 预期：抛出 EditorException，消息包含 "列号越界"。
     */
    @Test
    void testDeleteColumnOutOfBounds() {
        editor.append("Hello");
        EditorException exception = assertThrows(EditorException.class, () -> {
            editor.delete(1, 10, 1); // 列号越界
        });
        assertTrue(exception.getMessage().contains("列号越界"));
    }
    
    // ========== Replace 测试 ==========
    
    /**
     * 测试 replace 替换文本。
     * 测试数据：追加 "Hello World"，然后替换第1行第1列开始5个字符为 "Hi"。
     * 预期：结果为 "Hi World"，修改状态为 true。
     */
    @Test
    void testReplace() throws EditorException {
        editor.append("Hello World");
        editor.replace(1, 1, 5, "Hi");
        List<String> lines = editor.show();
        assertEquals("Hi World", lines.get(0));
        assertTrue(editor.isModified());
    }
    
    /**
     * 测试 replace 为更长文本。
     * 测试数据：追加 "Hi"，然后替换第1行第1列开始2个字符为 "Hello"。
     * 预期：结果为 "Hello"。
     */
    @Test
    void testReplaceWithLongerText() throws EditorException {
        editor.append("Hi");
        editor.replace(1, 1, 2, "Hello");
        List<String> lines = editor.show();
        assertEquals("Hello", lines.get(0));
    }
    
    /**
     * 测试 replace 为更短文本（删除）。
     * 测试数据：追加 "Hello World"，然后替换第1行第1列开始5个字符为空字符串。
     * 预期：结果为 " World"。
     */
    @Test
    void testReplaceWithEmpty() throws EditorException {
        editor.append("Hello World");
        editor.replace(1, 1, 5, ""); // 替换为空字符串，等同于删除
        List<String> lines = editor.show();
        assertEquals(" World", lines.get(0));
    }
    
    /**
     * 测试 replace 包含换行符。
     * 测试数据：追加 "Line1"，然后替换第1行第5列开始0个字符为 "\nLine2"。
     * 预期：行数为2，第一行为 "Line"，第二行为 "Line21"。
     */
    @Test
    void testReplaceWithNewline() throws EditorException {
        editor.append("Line1");
        editor.replace(1, 5, 0, "\nLine2");
        List<String> lines = editor.show();
        assertEquals(2, lines.size());
        assertEquals("Line", lines.get(0));
        assertEquals("Line21", lines.get(1));
    }
    
    /**
     * 测试 replace 行号越界。
     * 测试数据：追加 "Hello"，然后替换第2行第1列开始1个字符为 "text"。
     * 预期：抛出 EditorException，消息包含 "行号越界"。
     */
    @Test
    void testReplaceOutOfBounds() {
        editor.append("Hello");
        EditorException exception = assertThrows(EditorException.class, () -> {
            editor.replace(2, 1, 1, "text"); // 行号越界
        });
        assertTrue(exception.getMessage().contains("行号越界"));
    }
    
    // ========== Show 测试 ==========
    
    /**
     * 测试 show 空编辑器。
     * 测试数据：无。
     * 预期：返回空列表。
     */
    @Test
    void testShowEmpty() {
        List<String> lines = editor.show();
        assertTrue(lines.isEmpty());
    }
    
    /**
     * 测试 show 所有行。
     * 测试数据：追加 "Line 1"、"Line 2"、"Line 3"。
     * 预期：返回3行，内容依次为 "Line 1"、"Line 2"、"Line 3"。
     */
    @Test
    void testShowAll() {
        editor.append("Line 1");
        editor.append("Line 2");
        editor.append("Line 3");
        List<String> lines = editor.show();
        assertEquals(3, lines.size());
        assertEquals("Line 1", lines.get(0));
        assertEquals("Line 2", lines.get(1));
        assertEquals("Line 3", lines.get(2));
    }
    
    /**
     * 测试 show 指定范围。
     * 测试数据：追加4行，然后 show(2, 3)。
     * 预期：返回2行，第1行为 "Line 2"，第2行为 "Line 3"。
     */
    @Test
    void testShowRange() {
        editor.append("Line 1");
        editor.append("Line 2");
        editor.append("Line 3");
        editor.append("Line 4");
        List<String> lines = editor.show(2, 3);
        assertEquals(2, lines.size());
        assertEquals("Line 2", lines.get(0));
        assertEquals("Line 3", lines.get(1));
    }
    
    /**
     * 测试 show 单行。
     * 测试数据：追加2行，然后 show(1, 1)。
     * 预期：返回1行，内容为 "Line 1"。
     */
    @Test
    void testShowSingleLine() {
        editor.append("Line 1");
        editor.append("Line 2");
        List<String> lines = editor.show(1, 1);
        assertEquals(1, lines.size());
        assertEquals("Line 1", lines.get(0));
    }
    
    /**
     * 测试 show 超出范围。
     * 测试数据：追加1行，然后 show(10, 20)。
     * 预期：返回空列表。
     */
    @Test
    void testShowOutOfRange() {
        editor.append("Line 1");
        List<String> lines = editor.show(10, 20);
        assertTrue(lines.isEmpty());
    }
    
    // ========== Undo/Redo 测试 ==========
    
    /**
     * 测试 undo 撤销操作。
     * 测试数据：追加 "Line 1"和"Line 2"，然后 undo。
     * 预期：行数为1，内容为 "Line 1"，canRedo 为 true。
     */
    @Test
    void testUndo() {
        editor.append("Line 1");
        editor.append("Line 2");
        assertTrue(editor.canUndo());
        
        editor.undo();
        List<String> lines = editor.show();
        assertEquals(1, lines.size());
        assertEquals("Line 1", lines.get(0));
        assertTrue(editor.canRedo());
    }
    
    /**
     * 测试 redo 重做操作。
     * 测试数据：追加 "Line 1"和"Line 2"，undo，然后 redo。
     * 预期：行数为2，内容为 "Line 1"和"Line 2"。
     */
    @Test
    void testRedo() {
        editor.append("Line 1");
        editor.append("Line 2");
        editor.undo();
        
        editor.redo();
        List<String> lines = editor.show();
        assertEquals(2, lines.size());
        assertEquals("Line 1", lines.get(0));
        assertEquals("Line 2", lines.get(1));
    }
    
    /**
     * 测试 undo/redo 序列。
     * 测试数据：追加3行，undo 两次，redo 两次。
     * 预期：undo 后行数为1，redo 后行数为3。
     */
    @Test
    void testUndoRedoSequence() {
        editor.append("Line 1");
        editor.append("Line 2");
        editor.append("Line 3");
        
        editor.undo();
        editor.undo();
        assertEquals(1, editor.show().size());
        
        editor.redo();
        assertEquals(2, editor.show().size());
        
        editor.redo();
        assertEquals(3, editor.show().size());
    }
    
    /**
     * 测试新操作清除 redo 栈。
     * 测试数据：追加 "Line 1"，undo，然后追加 "New Line"。
     * 预期：canRedo 为 false。
     */
    @Test
    void testUndoAfterEdit() {
        editor.append("Line 1");
        editor.undo();
        assertEquals(0, editor.show().size());
        
        // 新操作应该清除redo栈
        editor.append("New Line");
        assertFalse(editor.canRedo());
    }
    
    /**
     * 测试空 undo 栈的 undo。
     * 测试数据：无。
     * 预期：canUndo 为 false，undo 不抛异常，内容为空。
     */
    @Test
    void testCannotUndoOnEmpty() {
        assertFalse(editor.canUndo());
        editor.undo(); // 应该不抛出异常
        assertTrue(editor.show().isEmpty());
    }
    
    /**
     * 测试空 redo 栈的 redo。
     * 测试数据：无。
     * 预期：canRedo 为 false，redo 不抛异常。
     */
    @Test
    void testCannotRedoOnEmpty() {
        assertFalse(editor.canRedo());
        editor.redo(); // 应该不抛出异常
    }
    
    // ========== Modified 状态测试 ==========
    
    /**
     * 测试修改状态。
     * 测试数据：初始 false，append 后 true，setModified(false) 后 false。
     * 预期：修改状态正确切换。
     */
    @Test
    void testModifiedStatus() {
        assertFalse(editor.isModified());
        editor.append("Line 1");
        assertTrue(editor.isModified());
        editor.setModified(false);
        assertFalse(editor.isModified());
    }
    
    // ========== FilePath 测试 ==========
    
    /**
     * 测试文件路径。
     * 测试数据：构造函数 "test.txt"，另一个 "another.txt"。
     * 预期：getFilePath 返回正确路径。
     */
    @Test
    void testFilePath() {
        assertEquals("test.txt", editor.getFilePath());
        TextEditor editor2 = new TextEditor("another.txt");
        assertEquals("another.txt", editor2.getFilePath());
    }
    
    // ========== 初始化测试 ==========
    
    /**
     * 测试带初始行的初始化。
     * 测试数据：初始行 ["Line 1", "Line 2", "Line 3"]。
     * 预期：行数为3，内容正确，修改状态为 false。
     */
    @Test
    void testInitWithLines() {
        List<String> initialLines = Arrays.asList("Line 1", "Line 2", "Line 3");
        TextEditor editor2 = new TextEditor("test.txt", initialLines);
        List<String> lines = editor2.show();
        assertEquals(3, lines.size());
        assertEquals("Line 1", lines.get(0));
        assertEquals("Line 2", lines.get(1));
        assertEquals("Line 3", lines.get(2));
        assertFalse(editor2.isModified());
    }
    
    /**
     * 测试 null 初始行的初始化。
     * 测试数据：null。
     * 预期：内容为空。
     */
    @Test
    void testInitWithNullLines() {
        TextEditor editor2 = new TextEditor("test.txt", null);
        assertTrue(editor2.show().isEmpty());
    }
}
