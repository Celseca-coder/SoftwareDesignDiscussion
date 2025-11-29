import { Command } from './Command';
import { Editor } from '../editor/Editor';

export class CommandManager {
    private undoStack: Command[] = [];
    private redoStack: Command[] = [];
    private editor: Editor;

    constructor(editor: Editor) {
        this.editor = editor;
    }

    clear(): void {
        this.undoStack = [];
        this.redoStack = [];
    }

    canUndo(): boolean { return this.undoStack.length > 0; }
    canRedo(): boolean { return this.redoStack.length > 0; }

    
    execute(cmd: Command): void {
        try {
            cmd.execute(this.editor);
            this.undoStack.push(cmd);
            this.redoStack = [];
        } catch (err) {
            console.error(err);
        }
    }

    undo(): Command | null {
        const cmd = this.undoStack.pop();
        if (!cmd) return null;
        cmd.undo(this.editor);
        this.redoStack.push(cmd);
        return cmd;
    }

    redo(): Command | null {
        const cmd = this.redoStack.pop();
        if (!cmd) return null;
        cmd.execute(this.editor);
        this.undoStack.push(cmd);
        return cmd;
    }
}
