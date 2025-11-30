import { TextEditor } from "../../src/editor/TextEditor";
import { EventBus } from "../../src/core/EventBus";
import * as fs from 'fs';

describe("TextEditor Command Tests", () => {
    let editor: TextEditor;
    let eventBus: EventBus;

    beforeEach(() => {
        eventBus = new EventBus();
        editor = TextEditor.create("test/.testfile/TextEditorTest.txt", eventBus);
    });

    it("should set content correctly", () => {
        editor.setContent(["Line 1", "Line 2", "Line 3"]);
        expect(editor.getContent()).toBe("Line 1\nLine 2\nLine 3");
    });

    it("should append a line", () => {
        editor.appendLine("First line");
        expect(editor.getContent()).toBe("First line");
    });

    it("should delete the last line", () => {
        editor.appendLine("Line 1");
        editor.appendLine("Line 2");
        editor.deleteLastLine();
        expect(editor.getContent()).toBe("Line 1");
    });

    it("should insert text at specific position", () => {
        editor.appendLine("Hello World");
        editor.insertAt(0, 6, "Beautiful ");
        expect(editor.getContent()).toBe("Hello Beautiful World");
    });

    it("should delete text at specific position", () => {
        editor.appendLine("Hello Beautiful World");
        editor.deleteAt(0, 6, 10);
        expect(editor.getContent()).toBe("Hello World");
    });

    it("should handle out of bounds insert", () => {
        expect(() => {
            editor.insertAt(1, 0, "This should fail");
        }).toThrow();
    });

    it("should handle out of bounds delete", () => {
        editor.appendLine("Short line");
        expect(() => {
            editor.deleteAt(0, 5, 10);
        }).toThrow();
    });

    it("should insert at end of file", () => {
        editor.insertAt(0, 0, "Start");
        editor.insertAt(1, 0, "End");
        expect(editor.getContent()).toBe("Start\nEnd");
    });

    it("should allow inserting multiple lines", () => {
        editor.insertAt(0, 0, "Line1\nLine2\nLine3");
        expect(editor.getContent()).toBe("Line1\nLine2\nLine3");
    });

    it("should throw error when inserting in empty file at non-zero position", () => {
        expect(() => {
            editor.insertAt(0, 1, "Fail");
        }).toThrow();
    });

    it("should emit event and generate file on save", () => {
        const spy = jest.spyOn(eventBus, 'emit');
        editor.appendLine("Some content");
        editor.save();
        expect(spy).toHaveBeenCalledWith('command-execute', expect.objectContaining({
            log: 'save'
        }));
        const fileContent = fs.readFileSync("test/.testfile/TextEditorTest.txt", 'utf-8');
        expect(fileContent).toBe("Some content");

        // Clean up
        fs.unlinkSync("test/.testfile/TextEditorTest.txt");
    });
});