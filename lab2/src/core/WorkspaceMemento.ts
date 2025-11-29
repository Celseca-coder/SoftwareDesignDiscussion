import { EditorInfo } from "../editor/Editor"
import * as fs from 'fs';

export type WorkspaceState = {
    openFiles: Array<EditorInfo>;
    activeEditorId: string | null;
}

export class WorkspaceMemento {
    static savePath = '.editor_workspace';
    private state: WorkspaceState;

    constructor(state: WorkspaceState){
        this.state = state;
    }

    getState(): WorkspaceState {
        return this.state;
    }

    serialize(){
        return JSON.stringify(this.state, null, 2);
    }

    save(){
        fs.writeFileSync(WorkspaceMemento.savePath, this.serialize(), 'utf-8');
    }

    static deserialize(raw: string): WorkspaceState {
        return JSON.parse(raw) as WorkspaceState;
    }

    static load(): WorkspaceMemento | null {
        if (!fs.existsSync(this.savePath)) return null;
        const raw = fs.readFileSync(this.savePath, 'utf-8');
        const state = this.deserialize(raw);
        return new WorkspaceMemento(state);
    }
}
