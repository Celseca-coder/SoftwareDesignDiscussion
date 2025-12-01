package lab1.domain.editor;


import lab1.domain.command.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// 目标：测试 TextEditor 和 Command 的核心逻辑 (纯单元测试)
// TDD.pdf 原则：测试是独立的 [cite: 670]
class TextEditorTest {

    private TextEditor editor;

    @BeforeEach
    void setUp() {
        // 每个测试都在一个干净的 "Hello" 编辑器实例上运行
        editor = new TextEditor("test.txt", "Hello");
    }

    @Test
    void testInsertCommand() {
        ICommand insert = new InsertCommand(editor, 1, 6, " World");
        editor.executeCommand(insert);
        assertEquals("Hello World", editor.getLines().get(0));
    }

    @Test
    void testAppendCommand() {
        ICommand append = new AppendCommand(editor, "New Line");
        editor.executeCommand(append);
        assertEquals(2, editor.getLines().size());
        assertEquals("Hello", editor.getLines().get(0));
        assertEquals("New Line", editor.getLines().get(1));
    }

    @Test
    void testDeleteCommand() {
        // "Hello" 的长度是 5。测试删除 "Hello"
        ICommand deleteCmd = new DeleteCommand(editor, 1, 1, 5);
        editor.executeCommand(deleteCmd);

        // 断言：内容现在应该是空字符串
        assertEquals("", editor.getLines().get(0));
    }

    @Test
    void testDeleteCommand_ThrowsException_WhenLengthExceeds() {
        // 准备一个删除 6 个字符的命令 (从只有 5 个字符的 "Hello" 中)
        ICommand deleteCmd = new DeleteCommand(editor, 1, 1, 6);

        // 断言：当执行这个命令时，*必须* 抛出 IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            editor.executeCommand(deleteCmd);
        });

        // 验证：抛出异常后，内容保持不变
        assertEquals("Hello", editor.getLines().get(0));
    }

    @Test
    void testReplaceCommand() {
        // 将 "Hello" 替换为 "Hi"
        ICommand replaceCmd = new ReplaceCommand(editor, 1, 1, 5, "Hi");
        editor.executeCommand(replaceCmd);
        assertEquals("Hi", editor.getLines().get(0));
    }

    @Test
    void testUndoRedo() {
        // 初始状态
        assertEquals("Hello", editor.getLines().get(0));

        // 执行命令
        ICommand insert = new InsertCommand(editor, 1, 6, " World");
        editor.executeCommand(insert);
        assertEquals("Hello World", editor.getLines().get(0));

        // 撤销 (Undo)
        editor.undo();
        assertEquals("Hello", editor.getLines().get(0)); // 恢复原状
        assertTrue(editor.canRedo());

        // 重做 (Redo)
        editor.redo();
        assertEquals("Hello World", editor.getLines().get(0)); // 恢复执行
    }
}