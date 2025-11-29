import { Command } from '../../src/commands/Command';
import { CommandManager } from '../../src/commands/CommandManager';
import { EventBus } from '../../src/core/EventBus';
import { Editor } from '../../src/editor/Editor';

describe('CommandManager', () => {
    let editor: Editor;
    let commandManager: CommandManager;
    let mockCommand: Command;

    class MockEditor extends Editor {
        constructor() {
            super({ id: 'mock-editor', path: 'test/.testfile/cmdManager.txt', modified: false, loggingEnabled: true }, new EventBus());
        }
        save(): void {}
        getContent(): string { return ""; }
    }

    beforeEach(() => {
        editor = new MockEditor();
        commandManager = new CommandManager(editor);
        mockCommand = {
            execute: jest.fn(),
            undo: jest.fn(),
            toLog: () => 'MockCommand'
        };
    });

    it('should execute a command and add it to the undo stack', () => {
        commandManager.execute(mockCommand);
        expect(mockCommand.execute).toHaveBeenCalledWith(editor);
        expect(commandManager.canUndo()).toBe(true);
        expect(commandManager.canRedo()).toBe(false);
    });

    it('should undo a command and move it to the redo stack', () => {
        commandManager.execute(mockCommand);
        const undoneCommand = commandManager.undo();
        expect(undoneCommand).toBe(mockCommand);
        expect(mockCommand.undo).toHaveBeenCalledWith(editor);
        expect(commandManager.canUndo()).toBe(false);
        expect(commandManager.canRedo()).toBe(true);
    });

    it('should redo a command and move it back to the undo stack', () => {
        commandManager.execute(mockCommand);
        commandManager.undo();
        const redoneCommand = commandManager.redo();
        expect(redoneCommand).toBe(mockCommand);
        expect(mockCommand.execute).toHaveBeenCalledTimes(2); // once on execute, once on redo
        expect(commandManager.canUndo()).toBe(true);
        expect(commandManager.canRedo()).toBe(false);
    });

    it('should clear both undo and redo stacks', () => {
        commandManager.execute(mockCommand);
        commandManager.undo();
        commandManager.clear();
        expect(commandManager.canUndo()).toBe(false);
        expect(commandManager.canRedo()).toBe(false);
    });

    it('should handle error during command execution gracefully', () => {
        const errorCommand: Command = {
            execute: jest.fn(() => { throw new Error('Execution failed'); }),
            undo: jest.fn(),
            toLog: () => 'ErrorCommand'
        };
        console.error = jest.fn();
        commandManager.execute(errorCommand);
        expect(console.error).toHaveBeenCalled();
        expect(commandManager.canUndo()).toBe(false);
        expect(commandManager.canRedo()).toBe(false);
    });
});
