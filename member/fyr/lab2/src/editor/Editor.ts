import { CommandManager } from "../commands/CommandManager";
import { Command } from '../commands/Command';
import { EventBus } from "../core/EventBus";
import * as fs from 'fs';

export interface EditorInfo {
    type: string;
    id: string;
    path: string;
    modified: boolean;
    loggingEnabled: boolean;
}

export abstract class Editor {
    info: EditorInfo;
    protected commandManager: CommandManager;
    protected eventBus: EventBus;

    protected constructor(info: EditorInfo, eventBus: EventBus) {
        this.info = info;
        this.eventBus = eventBus;
        this.commandManager = new CommandManager(this);
    }
    
    executeCommand(cmd: Command): void {
        this.commandManager.execute(cmd);
        this.info.modified = true;
        this.eventBus.emit('command-execute', { editorInfo: this.info, log: cmd.toLog() });
    }

    undoCommand(): void {
        const cmd = this.commandManager.undo();
        if (cmd) {
            this.info.modified = true;
            this.eventBus.emit('command-execute', { editorInfo: this.info, log: `undo ${cmd.toLog()}` });
        } else {
            console.warn("没有可撤销的命令。");
        }
    }

    redoCommand(): void {
        const cmd = this.commandManager.redo();
        if (cmd) {
            this.info.modified = true;
            this.eventBus.emit('command-execute', { editorInfo: this.info, log: `redo ${cmd.toLog()}` });
        } else {
            console.warn("没有可重做的命令。");
        }
    }

    save(): void {
        try {
            fs.writeFileSync(this.info.path, this.getContent(), 'utf-8');
            this.info.modified = false;
            this.eventBus.emit('command-execute', { editorInfo: this.info, log: `save` });
        } catch (err) {
            if (err instanceof Error && err.message.includes('ENOENT'))
                console.error("目录不存在，无法保存文件: " + this.info.path);
            else
                console.error("保存失败: " + this.info.path, err);
        }
    }

    abstract getContent(): string;
    abstract doSpellCheck(): void;
    abstract logInit(): void;
}
