package com.editor.core.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CommandManager 单元测试
 * 测试命令管理器的undo/redo功能
 */
public class CommandManagerTest {
    private CommandManager commandManager;
    private TestWorkspace workspace;
    
    @BeforeEach
    void setUp() {
        commandManager = new CommandManager();
        workspace = new TestWorkspace();
    }
    
    // ========== 基本执行测试 ==========
    
    /**
     * 测试普通命令的执行。
     * 创建并执行 TestCommand，验证 executed 标志为 true。
     */
    @Test
    void testExecuteCommand() throws CommandException {
        TestCommand command = new TestCommand("test");
        commandManager.executeCommand(command);
        assertTrue(command.executed);
    }
    
    /**
     * 测试可撤销命令的执行和入栈。
     * 执行 TestUndoableCommand，验证 executed 为 true，且 canUndo 为 true。
     */
    @Test
    void testExecuteUndoableCommand() throws CommandException {
        TestUndoableCommand command = new TestUndoableCommand("test", true);
        commandManager.executeCommand(command);
        assertTrue(command.executed);
        assertTrue(commandManager.canUndo());
    }
    
    // ========== Undo测试 ==========
    
    /**
     * 测试撤销单个可撤销命令。
     * 执行可撤销命令后撤销，验证 undone 标志为 true。
     */
    @Test
    void testUndo() throws CommandException {
        TestUndoableCommand command = new TestUndoableCommand("test", true);
        commandManager.executeCommand(command);
        
        commandManager.undo();
        assertTrue(command.undone);
    }
    
    /**
     * 测试不可撤销命令不入撤销栈。
     * 执行普通命令，验证 canUndo 为 false。
     */
    @Test
    void testUndoNonUndoable() throws CommandException {
        TestCommand command = new TestCommand("test");
        commandManager.executeCommand(command);
        
        // 不可撤销的命令不应该进入undo栈
        assertFalse(commandManager.canUndo());
    }
    
    /**
     * 测试空撤销栈时的异常。
     * 直接撤销，验证抛出 CommandException。
     */
    @Test
    void testUndoEmpty() {
        assertFalse(commandManager.canUndo());
        assertThrows(CommandException.class, () -> {
            commandManager.undo();
        });
    }
    
    /**
     * 测试多命令的 LIFO 撤销。
     * 执行三个命令后撤销一次，验证最后执行的命令被撤销。
     */
    @Test
    void testUndoMultiple() throws CommandException {
        TestUndoableCommand cmd1 = new TestUndoableCommand("cmd1", true);
        TestUndoableCommand cmd2 = new TestUndoableCommand("cmd2", true);
        TestUndoableCommand cmd3 = new TestUndoableCommand("cmd3", true);
        
        commandManager.executeCommand(cmd1);
        commandManager.executeCommand(cmd2);
        commandManager.executeCommand(cmd3);
        
        commandManager.undo();
        assertTrue(cmd3.undone);
        assertFalse(cmd2.undone);
        assertFalse(cmd1.undone);
    }
    
    // ========== Redo测试 ==========
    
    /**
     * 测试撤销后的重做。
     * 执行命令、撤销后重做，验证 executeCount 增加。
     */
    @Test
    void testRedo() throws CommandException {
        TestUndoableCommand command = new TestUndoableCommand("test", true);
        commandManager.executeCommand(command);
        commandManager.undo();
        
        commandManager.redo();
        assertEquals(2, command.executeCount);
    }
    
    /**
     * 测试空重做栈时的异常。
     * 直接重做，验证抛出 CommandException。
     */
    @Test
    void testRedoEmpty() {
        assertFalse(commandManager.canRedo());
        assertThrows(CommandException.class, () -> {
            commandManager.redo();
        });
    }
    
    /**
     * 测试新命令执行后重做栈被清空。
     * 撤销后执行新命令，验证 canRedo 为 false。
     */
    @Test
    void testRedoClearedOnNewCommand() throws CommandException {
        TestUndoableCommand cmd1 = new TestUndoableCommand("cmd1", true);
        TestUndoableCommand cmd2 = new TestUndoableCommand("cmd2", true);
        
        commandManager.executeCommand(cmd1);
        commandManager.undo();
        assertTrue(commandManager.canRedo());
        
        commandManager.executeCommand(cmd2);
        assertFalse(commandManager.canRedo());
    }
    
    // ========== 辅助测试类 ==========
    
    static class TestCommand implements Command {
        private String name;
        boolean executed = false;
        
        TestCommand(String name) {
            this.name = name;
        }
        
        @Override
        public void execute() throws CommandException {
            executed = true;
        }
        
        @Override
        public String getCommandName() {
            return name;
        }
        
        @Override
        public String getDescription() {
            return "Test command";
        }
    }
    
    static class TestUndoableCommand implements UndoableCommand {
        private String name;
        private boolean undoable;
        boolean executed = false;
        boolean undone = false;
        int executeCount = 0;
        
        TestUndoableCommand(String name, boolean undoable) {
            this.name = name;
            this.undoable = undoable;
        }
        
        @Override
        public void execute() throws CommandException {
            executed = true;
            executeCount++;
        }
        
        @Override
        public void undo() throws CommandException {
            undone = true;
        }
        
        @Override
        public boolean isUndoable() {
            return undoable;
        }
        
        @Override
        public String getCommandName() {
            return name;
        }
        
        @Override
        public String getDescription() {
            return "Test undoable command";
        }
    }
    
    static class TestWorkspace {
        // 测试用的工作区
    }
}
