package editor.command;

import editor.core.TextEditor;
import editor.workspace.Workspace;
import editor.command.workspace.*;
import editor.command.text.*;
import editor.command.log.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 各种命令的集成测试
 */
public class CommandTest {
    private Workspace workspace;
    private String testFile = "test_command.txt";

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
            Files.deleteIfExists(Paths.get(testFile));
            Files.deleteIfExists(Paths.get("." + testFile + ".log"));
            Files.deleteIfExists(Paths.get("test2.txt"));
        } catch (Exception e) {
            // 忽略
        }
    }

    // ========== 工作区命令测试 ==========

    @Test
    public void testInitCommand() {
        InitCommand cmd = new InitCommand(workspace, testFile, false);
        cmd.execute();
        
        assertNotNull("文件应该被创建", workspace.getEditor(testFile));
        assertEquals("应该成为活动文件", testFile, workspace.getActiveEditor().getFilePath());
    }

    @Test
    public void testInitCommandWithLog() {
        InitCommand cmd = new InitCommand(workspace, testFile, true);
        cmd.execute();
        
        TextEditor editor = (TextEditor) workspace.getEditor(testFile);
        assertNotNull("文件应该被创建", editor);
        assertFalse("应该有内容", editor.getLines().isEmpty());
        assertEquals("第一行应该是 # log", "# log", editor.getLines().get(0));
    }

    @Test
    public void testLoadCommand() throws Exception {
        // 先创建文件
        Files.write(Paths.get(testFile), "测试内容".getBytes("UTF-8"));
        
        LoadCommand cmd = new LoadCommand(workspace, testFile);
        cmd.execute();
        
        assertNotNull("文件应该被加载", workspace.getEditor(testFile));
    }

    @Test
    public void testSaveCommand() throws Exception {
        // 先创建编辑器
        TextEditor editor = new TextEditor(testFile);
        editor.append("测试内容");
        workspace.addEditor(testFile, editor);
        
        SaveCommand cmd = new SaveCommand(workspace, null);
        cmd.execute();
        
        assertTrue("文件应该存在", new File(testFile).exists());
    }

    @Test
    public void testEditCommand() {
        TextEditor editor1 = new TextEditor(testFile);
        TextEditor editor2 = new TextEditor("test2.txt");
        
        workspace.addEditor(testFile, editor1);
        workspace.addEditor("test2.txt", editor2);
        
        EditCommand cmd = new EditCommand(workspace, testFile);
        cmd.execute();
        
        assertEquals("应该切换到指定文件", editor1, workspace.getActiveEditor());
    }

    @Test
    public void testCloseCommand() {
        TextEditor editor = new TextEditor(testFile);
        workspace.addEditor(testFile, editor);
        
        CloseCommand cmd = new CloseCommand(workspace, testFile);
        cmd.execute();
        
        assertNull("文件应该被关闭", workspace.getEditor(testFile));
    }

    @Test
    public void testEditorListCommand() {
        TextEditor editor = new TextEditor(testFile);
        workspace.addEditor(testFile, editor);
        
        EditorListCommand cmd = new EditorListCommand(workspace);
        cmd.execute(); // 应该不抛出异常
    }

    @Test
    public void testUndoCommand() {
        TextEditor editor = new TextEditor(testFile);
        workspace.addEditor(testFile, editor);
        
        UndoCommand cmd = new UndoCommand(workspace);
        cmd.execute(); // 应该不抛出异常
    }

    @Test
    public void testRedoCommand() {
        TextEditor editor = new TextEditor(testFile);
        workspace.addEditor(testFile, editor);
        
        RedoCommand cmd = new RedoCommand(workspace);
        cmd.execute(); // 应该不抛出异常
    }

    // ========== 文本编辑命令测试 ==========

    @Test
    public void testAppendTextCommand() {
        TextEditor editor = new TextEditor(testFile);
        workspace.addEditor(testFile, editor);
        
        AppendTextCommand cmd = new AppendTextCommand(workspace, "新行");
        cmd.execute();
        
        assertEquals("应该添加一行", 1, editor.getLines().size());
        assertEquals("内容应该匹配", "新行", editor.getLines().get(0));
    }

    @Test
    public void testInsertTextCommand() {
        TextEditor editor = new TextEditor(testFile);
        editor.append("world");
        workspace.addEditor(testFile, editor);
        
        InsertTextCommand cmd = new InsertTextCommand(workspace, 1, 1, "Hello ");
        cmd.execute();
        
        assertEquals("应该插入文本", "Hello world", editor.getLines().get(0));
    }

    @Test
    public void testDeleteTextCommand() {
        TextEditor editor = new TextEditor(testFile);
        editor.append("Hello world");
        workspace.addEditor(testFile, editor);
        
        DeleteTextCommand cmd = new DeleteTextCommand(workspace, 1, 7, 5);
        cmd.execute();
        
        assertEquals("应该删除文本", "Hello ", editor.getLines().get(0));
    }

    @Test
    public void testReplaceTextCommand() {
        TextEditor editor = new TextEditor(testFile);
        editor.append("fast fox");
        workspace.addEditor(testFile, editor);
        
        ReplaceTextCommand cmd = new ReplaceTextCommand(workspace, 1, 1, 4, "slow");
        cmd.execute();
        
        assertEquals("应该替换文本", "slow fox", editor.getLines().get(0));
    }

    @Test
    public void testShowCommand() {
        TextEditor editor = new TextEditor(testFile);
        editor.append("第一行");
        editor.append("第二行");
        workspace.addEditor(testFile, editor);
        
        ShowCommand cmd = new ShowCommand(workspace, -1, -1);
        cmd.execute(); // 应该不抛出异常
    }

    // ========== 日志命令测试 ==========

    @Test
    public void testLogOnCommand() {
        TextEditor editor = new TextEditor(testFile);
        workspace.addEditor(testFile, editor);
        
        LogOnCommand cmd = new LogOnCommand(workspace, null);
        cmd.execute();
        
        assertTrue("日志应该被启用", editor.isLoggingEnabled());
    }

    @Test
    public void testLogOffCommand() {
        TextEditor editor = new TextEditor(testFile);
        editor.enableLogging(workspace.getEventPublisher());
        workspace.addEditor(testFile, editor);
        
        LogOffCommand cmd = new LogOffCommand(workspace, null);
        cmd.execute();
        
        assertFalse("日志应该被关闭", editor.isLoggingEnabled());
    }

    @Test
    public void testLogShowCommand() {
        TextEditor editor = new TextEditor(testFile);
        editor.enableLogging(workspace.getEventPublisher());
        workspace.addEditor(testFile, editor);
        
        LogShowCommand cmd = new LogShowCommand(workspace, null);
        cmd.execute(); // 应该不抛出异常
    }

    // ========== EditorCommand (undo/redo) 测试 ==========

    @Test
    public void testAppendCommandUndoRedo() {
        TextEditor editor = new TextEditor(testFile);
        AppendCommand cmd = new AppendCommand(editor, "测试行");
        
        cmd.execute();
        assertEquals("执行后应该有一行", 1, editor.getLines().size());
        
        cmd.undo();
        assertEquals("撤销后应该没有行", 0, editor.getLines().size());
    }

    @Test
    public void testInsertCommandUndoRedo() {
        TextEditor editor = new TextEditor(testFile);
        editor.append("world");
        
        InsertCommand cmd = new InsertCommand(editor, 1, 1, "Hello ");
        cmd.execute();
        assertEquals("插入后", "Hello world", editor.getLines().get(0));
        
        cmd.undo();
        assertEquals("撤销后", "world", editor.getLines().get(0));
    }

    @Test
    public void testDeleteCommandUndoRedo() {
        TextEditor editor = new TextEditor(testFile);
        editor.append("Hello world");
        
        DeleteCommand cmd = new DeleteCommand(editor, 1, 7, 5);
        cmd.execute();
        assertEquals("删除后", "Hello ", editor.getLines().get(0));
        
        cmd.undo();
        assertEquals("撤销后", "Hello world", editor.getLines().get(0));
    }

    @Test
    public void testReplaceCommandUndoRedo() {
        TextEditor editor = new TextEditor(testFile);
        editor.append("fast fox");
        
        ReplaceCommand cmd = new ReplaceCommand(editor, 1, 1, 4, "slow");
        cmd.execute();
        assertEquals("替换后", "slow fox", editor.getLines().get(0));
        
        cmd.undo();
        assertEquals("撤销后", "fast fox", editor.getLines().get(0));
    }
}
