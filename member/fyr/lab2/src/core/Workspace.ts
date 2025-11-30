import { EventBus } from './EventBus';
import { Editor } from '../editor/Editor';
import { TextEditor } from '../editor/TextEditor';
import { XmlEditor } from '../editor/XmlEditor';
import { showDirTree } from '../utils/TreeViewer';
import { WorkspaceMemento } from './WorkspaceMemento';
import { Logger } from '../logging/Logger';
import { AppendCommand, InsertCommand, DeleteCommand, ReplaceCommand } from '../commands/TextCommands';
import { XmlInsertCommand, XmlAppendCommand, XmlEditIdCommand, XmlEditTextCommand, XmlDeleteCommand } from '../commands/XmlCommands';
import { rl } from '../utils/input';
import * as fs from 'fs';
import * as path from 'path';


function pathEqual(p1: string, p2: string): boolean {
    return path.resolve(p1) === path.resolve(p2);
}


export class Workspace {
    private eventBus = new EventBus();
    private editors = new Map<string, Editor>();
    private activeEditorId: string | null = null;
    private activeIdStack: Array<string> = [];
    private logger = new Logger(this.eventBus);

    // 用于确认文件类型并创建Editor
    private createEditor(file: string): Editor | null {
        const ext = path.extname(file).toLowerCase();
        if (ext === '.xml') return XmlEditor.create(file, this.eventBus);
        if (ext === '.txt') return TextEditor.create(file, this.eventBus);
        console.error(`不支持的文件类型: ${ext} （仅支持 .xml 和 .txt）`);
        return null;
    }

    private setActiveEditor(id: string | null): void {
        if (this.activeEditorId){
            this.activeIdStack = this.activeIdStack.filter(eid => eid !== id);
            this.activeIdStack.push(this.activeEditorId);
        }
        this.activeEditorId = id;

        console.log(`当前活动编辑器: ${id ? this.editors.get(id)!.info.path : '无'}`);
    }

    // 用于close()时切换到上一个活动编辑器
    private lastActiveId(): string | null {
        while (this.activeIdStack.length > 0) {
            const lastId = this.activeIdStack.pop()!;
            if (this.editors.has(lastId)) return lastId;
        }
        return null;
    }

    // 用于确认y/n
    private async confirmYesNo(question: string): Promise<boolean> {
        let answer = await rl.question(`${question} (y/n): `);
        while (answer !== 'y' && answer !== 'n') {
            answer = await rl.question("请输入 'y' 或 'n': ");
        }
        return answer === 'y';
    }

    private findEditorByPath(file: string): Editor | null {
        return Array.from(this.editors.values()).find(ed => pathEqual(ed.info.path, file)) || null;
    }

    // 用于寻找文件名对应的Editor
    private getEditor(file: string | undefined): Editor | null {
        if (!file) {
            if (!this.activeEditorId) {
                console.warn("没有活动编辑器。");
                return null;
            }
            return this.editors.get(this.activeEditorId)!;
        } else {
            const editor = this.findEditorByPath(file);
            if (!editor) {
                console.error("文件未打开: " + file);
                return null;
            }
            return editor;
        }
    }

    // 用于保存和恢复工作区状态（备忘录模式）
    private saveState(): void {
        const state = {
            openFiles: Array.from(this.editors.values()).map(ed => ed.info),
            activeEditorId: this.activeEditorId
        };
        new WorkspaceMemento(state).save();
    }

    private loadState(): void {
        const state = WorkspaceMemento.load()?.getState();
        if (!state) return;

        for (const fileInfo of state.openFiles) {
            const editor = fileInfo.type === 'xml'
                ? XmlEditor.create(fileInfo.path, this.eventBus)
                : TextEditor.create(fileInfo.path, this.eventBus);
            editor.info = fileInfo;
            this.editors.set(editor.info.id, editor);
        }
        this.setActiveEditor(state.activeEditorId);
    }



    constructor() {
        this.loadState();
    }

    load(file: string): void {
        // If Editor exists, set it as active
        const existingEditor = this.findEditorByPath(file);
        if (existingEditor) {
            this.setActiveEditor(existingEditor.info.id);
            return;
        }

        // Else, create a new Editor and set it as active
        const newEditor = this.createEditor(file);
        if (!newEditor) return;
        
        this.editors.set(newEditor.info.id, newEditor);
        this.setActiveEditor(newEditor.info.id);
        this.eventBus.emit('editor-start', { editorInfo: newEditor.info, log: `load ${file}` });
    }

    save(file: string | undefined): void {
        let editorsToSave: Editor[] = [];
        if (file === 'all') {
            if (this.editors.size === 0) console.warn("没有编辑器可保存。");
            editorsToSave = Array.from(this.editors.values());
        } else {
            const editor = this.getEditor(file);
            if (editor) editorsToSave = [editor];
        }

        for (let editor of editorsToSave) editor.save();
    }

    init(file: string, withLog: boolean = false): void {
        if (fs.existsSync(file) || this.findEditorByPath(file)) {
            console.error("文件已存在: " + file);
            return;
        }

        const newEditor = this.createEditor(file);
        if (!newEditor) return;

        if (withLog) newEditor.logInit();
        this.editors.set(newEditor.info.id, newEditor);
        this.setActiveEditor(newEditor.info.id);
        this.eventBus.emit('editor-start', { editorInfo: newEditor.info, log: `init ${file} ${withLog ? 'with-log' : ''}` });
    }

    async close(file: string | undefined): Promise<void> {
        let editorToClose: Editor | null = this.getEditor(file);
        if (!editorToClose) return;

        if (editorToClose.info.modified) {
            if (await this.confirmYesNo(`文件已修改，是否保存？`)) editorToClose.save();
        }

        this.eventBus.emit('command-execute', { editorInfo: editorToClose.info, log: `close` });
        this.editors.delete(editorToClose.info.id);
        this.setActiveEditor(this.lastActiveId());
    }

    edit(file: string): void {
        const editor = this.findEditorByPath(file);
        if (!editor) {
            console.error("文件未打开: " + file);
            return;
        }
        this.setActiveEditor(editor.info.id);
    }

    editorList(): void {
        let info = '';
        for (const editor of this.editors.values()) {
            const active = editor.info.id === this.activeEditorId ? '*' : ' ';
            const modified = editor.info.modified ? '[modified]' : '';
            info += `${active} ${editor.info.path} ${modified}\n`;
        }
        console.log(info);
    }

    dirTree(path: string = "."): void {
        showDirTree(path);
    }

    undo(): void {
        if (!this.activeEditorId) {
            console.warn("没有活动编辑器可撤销。");
            return;
        }

        const editor = this.editors.get(this.activeEditorId)!;
        editor.undoCommand();
    }

    redo(): void {
        if (!this.activeEditorId) {
            console.warn("没有活动编辑器可重做。");
            return;
        }

        const editor = this.editors.get(this.activeEditorId)!;
        editor.redoCommand();
    }

    async exit(): Promise<void> {
        for (const editor of this.editors.values()) {
            if (editor.info.modified) {
                if (await this.confirmYesNo(`文件 ${editor.info.path} 已修改，是否保存？`)) editor.save();
            }
        }

        this.saveState();

        rl.close();
        console.log("再见");
        process.exit(0);
    }


    logOn(file: string | undefined): void {
        let editor: Editor | null = this.getEditor(file);
        if (!editor) return;
        editor.info.loggingEnabled = true;
    }

    logOff(file: string | undefined): void {
        let editor: Editor | null = this.getEditor(file);
        if (!editor) return;
        editor.info.loggingEnabled = false;
    }

    logShow(file: string | undefined): void {
        let editor: Editor | null = this.getEditor(file);
        if (!editor) return;
        this.logger.showLog(editor.info.path);
    }

    spellCheck(file: string | undefined): void {
        let editor: Editor | null = this.getEditor(file);
        if (!editor) return;
        editor.doSpellCheck();
    }


    textEditorCommand(cmd: string, ...args: string[]): void {
        let editor: Editor | null = this.getEditor(undefined);
        if (!editor) return;
        if (!(editor instanceof TextEditor)) {
            console.error("当前活动编辑器不是文本编辑器。");
            return;
        }

        switch (cmd) {
            case 'append':
                editor.executeCommand(new AppendCommand(args[0]));
                break;

            case 'insert':
                const [lineStr, colStr] = args[0].split(':');
                editor.executeCommand(new InsertCommand(parseInt(lineStr), parseInt(colStr), args[1]));
                break;

            case 'delete':
                const [dLineStr, dColStr] = args[0].split(':');
                editor.executeCommand(new DeleteCommand(parseInt(dLineStr), parseInt(dColStr), parseInt(args[1])));
                break;

            case 'replace':
                const [rLineStr, rColStr] = args[0].split(':');
                editor.executeCommand(new ReplaceCommand(parseInt(rLineStr), parseInt(rColStr), parseInt(args[1]), args[2]));
                break;

            case 'show':
                const [startLine, endLine] = args.length >= 1
                    ? args[0].split(':').map(s => parseInt(s))
                    : [1, Infinity];
                editor.show(startLine, endLine);
                break;

            default:
                console.error("未知命令: " + cmd);
        }
    }

    xmlEditorCommand(cmd: string, ...args: string[]): void {
        let editor: Editor | null = this.getEditor(undefined);
        if (!editor) return;
        if (!(editor instanceof XmlEditor)) {
            console.error("当前活动编辑器不是XML编辑器。");
            return;
        }

        switch (cmd) {
            case 'insert-before':
                editor.executeCommand(new XmlInsertCommand(args[0], args[1], args[2], args[3]));
                break;

            case 'append-child':
                editor.executeCommand(new XmlAppendCommand(args[0], args[1], args[2], args[3]));
                break;

            case 'edit-id':
                editor.executeCommand(new XmlEditIdCommand(args[0], args[1]));
                break;

            case 'edit-text':
                editor.executeCommand(new XmlEditTextCommand(args[0], args[1]));
                break;

            case 'delete-element':
                editor.executeCommand(new XmlDeleteCommand(args[0]));
                break;
            
            default:
                console.error("未知命令: " + cmd);
        }
    }

    xmlShow(file: string | undefined): void {
        let editor: Editor | null = this.getEditor(file);
        if (!editor) return;
        if (!(editor instanceof XmlEditor)) {
            console.error("指定的编辑器不是XML编辑器。");
            return;
        }

        editor.showTree();
    }
}
