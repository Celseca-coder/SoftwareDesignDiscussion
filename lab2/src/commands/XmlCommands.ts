import { Command } from "./Command";
import { XmlElement, XmlEditor } from "../editor/XmlEditor";

export class XmlInsertCommand implements Command {
    constructor(
        private tag: string,
        private newId: string,
        private targetId: string,
        private text: string | null = null
    ) {};

    execute(editor: XmlEditor): void {
        editor.insertBefore(this.tag, this.newId, this.targetId, this.text);
    }

    undo(editor: XmlEditor): void {
        editor.delete(this.newId);
    }

    toLog(): string {
        let log = `insert-before ${this.tag} ${this.newId} ${this.targetId}`;
        if (this.text) log += ` "${this.text}"`;
        return log;
    }
}

export class XmlAppendCommand implements Command {
    constructor(
        private tag: string,
        private newId: string,
        private parentId: string,
        private text: string | null = null
    ) {}

    execute(editor: XmlEditor): void {
        editor.appendChild(this.tag, this.newId, this.parentId, this.text);
    }

    undo(editor: XmlEditor): void {
        editor.delete(this.newId);
    }

    toLog(): string {
        let log = `append-child ${this.tag} ${this.newId} ${this.parentId}`;
        if (this.text) log += ` "${this.text}"`;
        return log;
    }
}

export class XmlEditIdCommand implements Command {
    constructor(private oldId: string, private newId: string) {}

    execute(editor: XmlEditor): void {
        editor.editId(this.oldId, this.newId);
    }

    undo(editor: XmlEditor): void {
        editor.editId(this.newId, this.oldId);
    }

    toLog(): string {
        return `edit-id ${this.oldId} ${this.newId}`;
    }
}

export class XmlEditTextCommand implements Command {
    private originalText: string | null = null;
    constructor(private id: string, private newText: string | null) {}

    execute(editor: XmlEditor): void {
        this.originalText = editor.editText(this.id, this.newText);
    }

    undo(editor: XmlEditor): void {
        editor.editText(this.id, this.originalText);
    }

    toLog(): string {
        return `edit-text ${this.id} "${this.newText ?? ''}"`;
    }
}

export class XmlDeleteCommand implements Command {
    private deleted: {element: XmlElement, parentId: string, index: number} = null!;
    constructor(private id: string) {}

    execute(editor: XmlEditor): void {
        this.deleted = editor.delete(this.id);
    }

    undo(editor: XmlEditor): void {
        editor.undoDelete(this.deleted!.element, this.deleted!.parentId, this.deleted!.index);
    }

    toLog(): string {
        return `delete-element ${this.id}`;
    }
}
