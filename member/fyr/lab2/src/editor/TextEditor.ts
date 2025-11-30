import { EditorInfo, Editor } from './Editor'
import { EventBus } from "../core/EventBus";
import * as fs from 'fs';
import { randomUUID } from 'crypto';
import { checkSpell } from '../utils/checkSpell';

export class TextEditor extends Editor {
    private lines: string[] = [];

    static create(path: string, eventBus: EventBus): TextEditor {
        const fileExists = fs.existsSync(path);
        const initLines = fileExists ? fs.readFileSync(path, 'utf-8').split('\n') : [];
        const info: EditorInfo = {
            type: 'txt',
            id: randomUUID(),
            path,
            modified: !fileExists,
            loggingEnabled: initLines[0]?.startsWith("# log") || false
        };

        const editor = new TextEditor(info, eventBus);
        editor.lines = initLines;
        return editor;
    }

    getContent(): string {
        return this.lines.join('\n');
    }

    setContent(lines: string[]): void {
        this.lines = lines;
    }

    
    appendLine(line: string): void {
        if (line.includes('\n'))
            throw "append 不允许包含换行符";
        this.lines.push(line);
    }

    deleteLastLine(): void {
        this.lines.pop();
    }


    // 作业要求计数从1:1开始，太反直觉，这里内部仍然从0:0开始，Command中做转换
    private checkBounds(line: number, col: number, allowEnd: boolean): void {
        if (line < 0 || line >= this.lines.length + (allowEnd ? 1 : 0))
            throw "行号越界";
        if (col < 0 || col >= this.lines[line].length + (allowEnd ? 1 : 0))
            throw "列号越界";
    }

    // undo insert、undo delete需要考虑换行符，由于数据结构要求为string[]，
    // 算法边界条件太复杂，这里选择直接恢复原始文本
    insertAt(line: number, col: number, text: string): void {
        if (this.lines.length === 0 && (col !== 0 || line !== 0))
            throw "空文件只能在1:1位置插入";
        if (line === this.lines.length) this.lines.push('');
        this.checkBounds(line, col, true);

        const newLine = this.lines[line].slice(0, col) + text + this.lines[line].slice(col);
        const newLines = newLine.split('\n');
        this.lines.splice(line, 1, ...newLines);
    }

    show(startLine: number, endLine: number): void {
        if (startLine < 1) startLine = 1;
        if (endLine > this.lines.length) endLine = this.lines.length;
        if (startLine > endLine) {
            console.warn("起始行大于结束行，无法显示");
            return;
        }

        let report = '';
        for (let i = startLine - 1; i < endLine; i++) {
            report += `${i + 1}: ${this.lines[i]}\n`;
        }
        if (report.endsWith('\n')) report = report.slice(0, -1);
        console.log(report);
    }


    deleteAt(line: number, col: number, length: number): void {
        this.checkBounds(line, col, length === 0);
        if (col + length > this.lines[line].length)
            throw "删除长度超出行尾";

        const lineText = this.lines[line];
        const newLine = lineText.slice(0, col) + lineText.slice(col + length);
        this.lines[line] = newLine;
        if (newLine.length === 0) this.lines.splice(line, 1);
    }

    doSpellCheck(): void {
        type Suggestion = { line: number, col: number, word: string, suggestions: string[] };
        const suggestions: Suggestion[] = [];
        for (let i = 0; i < this.lines.length; i++) {
            const words = this.lines[i].split(/\W+/);
            let col = 0;
            for (const word of words) {
                if (word.length === 0) {
                    col += 1;
                    continue;
                }
                const result = checkSpell(word);
                if (result) {
                    suggestions.push({ line: i + 1, col: col + 1, word, suggestions: result });
                }
                col += word.length + 1;
            }
        }

        let report = `拼写检查结果:\n`;
        if (suggestions.length === 0) report += `未发现拼写错误。`;
        for (const s of suggestions) {
            report += `第${s.line}行，第${s.col}列: "${s.word}" -> `;
            report += s.suggestions.length > 0 ? `建议: ${s.suggestions.join(', ')}` : `找不到建议`;
            report += `\n`;
        }
        if (report.endsWith('\n')) report = report.slice(0, -1);
        console.log(report);
    }

    logInit(): void {
        this.appendLine("# log");
        this.info.loggingEnabled = true;
    }
}
