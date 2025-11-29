import { Command } from "./Command";
import { TextEditor } from "../editor/TextEditor";

export class AppendCommand implements Command {
    constructor(private line: string) { }

    execute(editor: TextEditor): void {
        editor.appendLine(this.line);
    }

    undo(editor: TextEditor): void {
        editor.deleteLastLine();
    }

    toLog(): string {
        return `append "${this.line}"`;
    }
}

export class InsertCommand implements Command {
    private originalLines: string[] = [];
    constructor(private line: number, private col: number, private text: string) { }

    execute(editor: TextEditor): void {
        this.originalLines = editor.getContent().split('\n');
        editor.insertAt(this.line - 1, this.col - 1, this.text);
    }

    undo(editor: TextEditor): void {
        editor.setContent(this.originalLines);
    }

    toLog(): string {
        return `insert ${this.line}:${this.col} "${this.text}"`;
    }
}

export class DeleteCommand implements Command {
    private originalLines: string[] = [];
    constructor(private line: number, private col: number, private length: number) { }

    execute(editor: TextEditor): void {
        this.originalLines = editor.getContent().split('\n');
        editor.deleteAt(this.line - 1, this.col - 1, this.length);
    }

    undo(editor: TextEditor): void {
        editor.setContent(this.originalLines);
    }

    toLog(): string {
        return `delete ${this.line}:${this.col} ${this.length}`;
    }
}

export class ReplaceCommand implements Command {
    private DeleteCmd: DeleteCommand;
    private InsertCmd: InsertCommand;

    constructor(private line: number, private col: number, private length: number, private newText: string) {
        this.DeleteCmd = new DeleteCommand(line, col, length);
        this.InsertCmd = new InsertCommand(line, col, newText);
    }

    execute(editor: TextEditor): void {
        this.DeleteCmd.execute(editor);
        this.InsertCmd.execute(editor);
    }

    undo(editor: TextEditor): void {
        this.InsertCmd.undo(editor);
        this.DeleteCmd.undo(editor);
    }

    toLog(): string {
        return `replace ${this.line}:${this.col} ${this.length} "${this.newText}"`;
    }
}
