import { Workspace } from '../core/Workspace';
import { rl } from '../utils/input';
import stringArgv from 'string-argv';
import chalk from 'chalk';
import '../utils/consoleColor';
import * as path from 'path';

console.log(chalk.blue("--- 软件设计 Lab2 多文件编辑器 ---"));

const ws = new Workspace();


async function runCommand(cmd: string, ...args: string[]): Promise<void> {
    switch (cmd) {
        case 'load':
            if (args.length < 1) {
                console.error('用法: load <file>');
                break;
            }
            ws.load(args[0]);
            break;

        case 'save':
            ws.save(args[0]);
            break;

        case 'init':
            if (args.length < 2 || !/^(text|xml)$/.test(args[0]) || (args.length >= 3 && args[2] !== 'with-log')) {
                console.error('用法: init <text|xml> <file> [with-log]');
                break;
            }
            if ((args[0] === 'text' && path.extname(args[1]).toLowerCase() !== '.txt') ||
                (args[0] === 'xml' && path.extname(args[1]).toLowerCase() !== '.xml')) {
                console.error(`文件扩展名与类型不匹配`);
                break;
            }
            ws.init(args[1], args[2] === 'with-log');
            break;

        case 'close':
            await ws.close(args[0]);
            break;

        case 'edit':
            if (args.length < 1) {
                console.error('用法: edit <file>');
                break;
            }
            ws.edit(args[0]);
            break;

        case 'editor-list':
            ws.editorList();
            break;

        case 'dir-tree':
            ws.dirTree(args[0]);
            break;

        case 'undo':
            ws.undo();
            break;

        case 'redo':
            ws.redo();
            break;

        case 'exit':
            await ws.exit();
            break;

        case 'log-on':
            ws.logOn(args[0]);
            break;

        case 'log-off':
            ws.logOff(args[0]);
            break;

        case 'log-show':
            ws.logShow(args[0]);
            break;

        case 'append':
            if (args.length < 1) {
                console.error('用法: append "text"');
                break;
            }
            ws.textEditorCommand(cmd, ...args);
            break;

        case 'insert':
            if (args.length < 2 || !/^\d+:\d+$/.test(args[0])) {
                console.error('用法: insert line:col "text"');
                break;
            }
            ws.textEditorCommand(cmd, ...args);;
            break;

        case 'delete':
            if (args.length < 2 || !/^\d+:\d+$/.test(args[0]) || isNaN(parseInt(args[1]))) {
                console.error('用法: delete line:col len');
                break;
            }
            ws.textEditorCommand(cmd, ...args);;
            break;

        case 'replace':
            if (args.length < 3 || !/^\d+:\d+$/.test(args[0]) || isNaN(parseInt(args[1]))) {
                console.error('用法: replace line:col len "text"');
                break;
            }
            ws.textEditorCommand(cmd, ...args);;
            break;

        case 'show':
            if (args.length >= 1 && !/^\d+:\d+$/.test(args[0])) {
                console.error('用法: show [start:end]');
                break;
            }
            ws.textEditorCommand(cmd, ...args);
            break;

        case 'insert-before':
            if (args.length < 3) {
                console.error('用法: insert-before <tagName> <newId> <targetId> ["text"]');
                break;
            }
            ws.xmlEditorCommand(cmd, ...args);;
            break;

        case 'append-child':
            if (args.length < 3) {
                console.error('用法: append-child <tagName> <newId> <parentId> ["text"]');
                break;
            }
            ws.xmlEditorCommand(cmd, ...args);;
            break;

        case 'edit-id':
            if (args.length < 2) {
                console.error('用法: edit-id <oldId> <newId>');
                break;
            }
            ws.xmlEditorCommand(cmd, ...args);;
            break;

        case 'edit-text':
            if (args.length < 1) {
                console.error('用法: edit-text <id> "text"');
                break;
            }
            ws.xmlEditorCommand(cmd, ...args);;
            break;
        
        case 'delete-element':
            if (args.length < 1) {
                console.error('用法: delete-element <elementId>');
                break;
            }
            ws.xmlEditorCommand(cmd, ...args);;
            break;

        case 'xml-tree':
            ws.xmlShow(args[0]);
            break;

        case 'spell-check':
            ws.spellCheck(args[0]);
            break;

        default:
            console.error("未知命令: " + cmd);
    }
}


(async function main() {
    while (true) {
        const input = await rl.question('> ');
        let [cmd, ...args] = stringArgv(input);
        if (!cmd) continue;

        // 处理转义字符
        cmd = cmd.toLowerCase().trim();
        args = args.map(arg => arg.replace(/\\n/g, '\n').replace(/\\t/g, '\t'));

        try {
            await runCommand(cmd, ...args);
        } catch (err) { console.error("错误: ", err); }
    }
})();
