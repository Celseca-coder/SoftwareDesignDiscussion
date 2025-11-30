import { EventBus } from "../core/EventBus";
import { nowTimestamp } from "../utils/time";
import * as fs from "fs";
import * as path from "path";

export class Logger {
    private eventBus: EventBus;
    private sessionStart = nowTimestamp();

    private getLogPath(srcPath: string): string {
        const srcFolder = path.dirname(srcPath);
        const srcFileName = path.basename(srcPath);
        return path.join(srcFolder, `.${srcFileName}.log`);
    }

    private appendLog(srcPath: string, line: string): void {
        const logPath = this.getLogPath(srcPath);
        try {
            fs.appendFileSync(logPath, line);
        } catch (err) {
            console.warn("无法写入日志: " + logPath, err);
        }
    }


    constructor(eventBus: EventBus) {
        this.eventBus = eventBus;
        this.eventBus.on("editor-start", this.onEditorStart.bind(this));
        this.eventBus.on("command-execute", this.onCommandExecute.bind(this));
    }

    private onEditorStart({ editorInfo, log }: any) {
        if (!editorInfo.loggingEnabled) return;
        const logContent = `session start at ${this.sessionStart}\n${nowTimestamp()} ${log}\n`;
        this.appendLog(editorInfo.path, logContent);
    }

    private onCommandExecute({ editorInfo, log }: any) {
        if (!editorInfo.loggingEnabled) return;
        const logContent = `${nowTimestamp()} ${log}\n`;
        this.appendLog(editorInfo.path, logContent);
    }

    showLog(srcPath: string): void {
        const logPath = this.getLogPath(srcPath);
        if (!fs.existsSync(logPath)) {
            console.warn("日志文件不存在: " + logPath);
            return;
        }
        const logContent = fs.readFileSync(logPath, 'utf-8');
        console.log(logContent);
    }
}
