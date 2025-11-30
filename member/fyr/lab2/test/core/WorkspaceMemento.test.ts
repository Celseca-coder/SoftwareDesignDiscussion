import { WorkspaceState, WorkspaceMemento } from "../../src/core/WorkspaceMemento";
import * as fs from 'fs';

describe('WorkspaceMemento', () => {
    const mockState: WorkspaceState = {
        openFiles: [
            { id: '1', path: '/path/to/file1.txt', modified: false, loggingEnabled: true },
            { id: '2', path: '/path/to/file2.txt', modified: true, loggingEnabled: false}
        ],
        activeEditorId: '1'
    };

    it('should serialize and deserialize state correctly', () => {
        const memento = new WorkspaceMemento(mockState);
        const serialized = memento.serialize();
        const deserializedState = WorkspaceMemento.deserialize(serialized);
        expect(deserializedState).toEqual(mockState);
    });

    it('should save and load state correctly', () => {
        WorkspaceMemento.savePath = 'test/.testfile/.workspaceMementoTest';
        const memento = new WorkspaceMemento(mockState);
        memento.save();
        expect(fs.existsSync(WorkspaceMemento.savePath)).toBe(true);
        const loadedMemento = WorkspaceMemento.load();
        expect(loadedMemento).not.toBeNull();
        expect(loadedMemento?.getState()).toEqual(mockState);

        // Clean up
        fs.unlinkSync(WorkspaceMemento.savePath);
    });
});
