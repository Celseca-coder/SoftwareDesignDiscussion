package editor.command;

import editor.workspace.Workspace;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * CommandParser 命令解析器测试
 */
public class CommandParserTest {
    private CommandParser parser;
    private Workspace workspace;

    @Before
    public void setUp() {
        workspace = new Workspace();
        parser = new CommandParser(workspace);
    }

    // ========== 工作区命令解析测试 ==========

    @Test
    public void testParseLoadCommand() {
        Command cmd = parser.parse("load test.txt");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseSaveCommand() {
        Command cmd = parser.parse("save");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseSaveWithFile() {
        Command cmd = parser.parse("save test.txt");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseSaveAll() {
        Command cmd = parser.parse("save all");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseInitCommand() {
        Command cmd = parser.parse("init test.txt");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseInitWithLog() {
        Command cmd = parser.parse("init test.txt with-log");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseCloseCommand() {
        Command cmd = parser.parse("close");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseEditCommand() {
        Command cmd = parser.parse("edit test.txt");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseEditorListCommand() {
        Command cmd = parser.parse("editor-list");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseDirTreeCommand() {
        Command cmd = parser.parse("dir-tree");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseDirTreeWithPath() {
        Command cmd = parser.parse("dir-tree /path/to/dir");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseUndoCommand() {
        Command cmd = parser.parse("undo");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseRedoCommand() {
        Command cmd = parser.parse("redo");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseExitCommand() {
        Command cmd = parser.parse("exit");
        assertNotNull("应该解析成功", cmd);
    }

    // ========== 文本编辑命令解析测试 ==========

    @Test
    public void testParseAppendCommand() {
        Command cmd = parser.parse("append \"text\"");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseInsertCommand() {
        Command cmd = parser.parse("insert 1:1 \"text\"");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseDeleteCommand() {
        Command cmd = parser.parse("delete 1:1 5");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseReplaceCommand() {
        Command cmd = parser.parse("replace 1:1 5 \"new\"");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseShowCommand() {
        Command cmd = parser.parse("show");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseShowWithRange() {
        Command cmd = parser.parse("show 1:10");
        assertNotNull("应该解析成功", cmd);
    }

    // ========== 日志命令解析测试 ==========

    @Test
    public void testParseLogOnCommand() {
        Command cmd = parser.parse("log-on");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseLogOffCommand() {
        Command cmd = parser.parse("log-off");
        assertNotNull("应该解析成功", cmd);
    }

    @Test
    public void testParseLogShowCommand() {
        Command cmd = parser.parse("log-show");
        assertNotNull("应该解析成功", cmd);
    }

    // ========== 引号处理测试 ==========

    @Test
    public void testParseQuotedText() {
        Command cmd = parser.parse("append \"带空格的文本\"");
        assertNotNull("应该正确解析带引号的文本", cmd);
    }

    @Test
    public void testParseMultipleQuotedArguments() {
        Command cmd = parser.parse("replace 1:1 5 \"新文本\"");
        assertNotNull("应该解析多个参数", cmd);
    }

    // ========== 错误处理测试 ==========

    @Test
    public void testParseUnknownCommand() {
        Command cmd = parser.parse("unknown-command");
        assertNull("未知命令应该返回 null", cmd);
    }

    @Test
    public void testParseEmptyString() {
        Command cmd = parser.parse("");
        assertNull("空字符串应该返回 null", cmd);
    }

    @Test
    public void testParseWhitespaceOnly() {
        Command cmd = parser.parse("   ");
        assertNull("只有空格应该返回 null", cmd);
    }

    @Test
    public void testParseIncompleteCommand() {
        Command cmd = parser.parse("load");
        assertNull("不完整的命令应该返回 null", cmd);
    }
}
