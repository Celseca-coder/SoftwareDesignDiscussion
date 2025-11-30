import { AppendCommand, InsertCommand, ReplaceCommand, DeleteCommand } from "../../src/commands/TextCommands";
import { TextEditor } from "../../src/editor/TextEditor";
import { EventBus } from "../../src/core/EventBus";

describe("TextCommand Tests", () => {
    let editor: TextEditor;
    let eventBus: EventBus;

    beforeEach(() => {
        eventBus = new EventBus();
        editor = TextEditor.create("test/.testfile/TextCommandsTest.txt", eventBus);
    });

    it("should append a line and undo", () => {
        const cmd = new AppendCommand("Hello, World!");
        editor.executeCommand(cmd);
        expect(editor.getContent()).toBe("Hello, World!");
        editor.undoCommand();
        expect(editor.getContent()).toBe("");
    });

    it("should insert text and undo", () => {
        editor.setContent(["Line 1", "Line 2"]);
        const cmd = new InsertCommand(1, 7, " Inserted");
        editor.executeCommand(cmd);
        expect(editor.getContent()).toBe("Line 1 Inserted\nLine 2");
        editor.undoCommand();
        expect(editor.getContent()).toBe("Line 1\nLine 2");
    });

    it("should delete text and undo", () => {
        editor.setContent(["Line 1", "Line 2"]);
        const cmd = new DeleteCommand(1, 6, 1);
        editor.executeCommand(cmd);
        expect(editor.getContent()).toBe("Line \nLine 2");
        editor.undoCommand();
        expect(editor.getContent()).toBe("Line 1\nLine 2");
    });

    it("should replace text and undo", () => {
        editor.setContent(["Line 1", "Line 2"]);
        const cmd = new ReplaceCommand(1, 6, 1, " Replaced");
        editor.executeCommand(cmd);
        expect(editor.getContent()).toBe("Line  Replaced\nLine 2");
        editor.undoCommand();
        expect(editor.getContent()).toBe("Line 1\nLine 2");
    });

    it("should redo after undo", () => {
        const cmd = new AppendCommand("Redo Test");
        editor.executeCommand(cmd);
        expect(editor.getContent()).toBe("Redo Test");
        editor.undoCommand();
        expect(editor.getContent()).toBe("");
        editor.redoCommand();
        expect(editor.getContent()).toBe("Redo Test");
    });

    it("should undo multiple times", () => {
        const cmd1 = new AppendCommand("First Line");
        const cmd2 = new AppendCommand("Second Line");
        editor.executeCommand(cmd1);
        editor.executeCommand(cmd2);
        expect(editor.getContent()).toBe("First Line\nSecond Line");
        editor.undoCommand();
        expect(editor.getContent()).toBe("First Line");
        editor.undoCommand();
        expect(editor.getContent()).toBe("");
    });

    it("should handle too much undo", () => {
        jest.spyOn(console, 'warn').mockImplementation(() => { });
        editor.undoCommand();
        expect(console.warn).toHaveBeenCalled();
    });
});
