import { Editor } from '../editor/Editor';

export interface Command {
    execute(editor: Editor): void;
    undo(editor: Editor): void;
    toLog(): string;
}