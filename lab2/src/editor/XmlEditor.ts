import { Editor, EditorInfo } from "./Editor";
import { EventBus } from "../core/EventBus";
import { randomUUID } from "crypto";
import * as fs from "fs";
import { NameProvider, TreeContentProvider, VisualTreeViewer } from "../utils/TreeViewer";
import { checkSpell } from "../utils/checkSpell";

const XML_DECLARATION = '<?xml version="1.0" encoding="UTF-8"?>\n';


function tokenize(xml: string): Array<any> {
    const tokens: any[] = [];
    const tagRegex = /<\/?[^>]+>|[^<]+/g;
    let match: RegExpExecArray | null;

    while ((match = tagRegex.exec(xml))) {
        const part = match[0];

        if (part.startsWith("</")) {
            const tag = part.slice(2, -1).trim();
            tokens.push({ type: 'close-tag', tag });
        }
        else if (part.startsWith("<")) {
            const content = part.slice(1, -1).trim();
            const [tag, ...rest] = content.split(/\s+/);

            const attr: Record<string, string> = {};
            const attrRegex = /(\w+)="([^"]*)"/g;
            let a: RegExpExecArray | null;

            const restText = rest.join(" ");
            while ((a = attrRegex.exec(restText))) {
                attr[a[1]] = a[2];
            }

            tokens.push({ type: 'open-tag', tag, attr });
        }
        else {
            tokens.push({ type: 'text', text: part });
        }
    }

    return tokens;
}

function parseXML(xml: string, idTable: Map<string, XmlElement>): XmlElement {
    xml = xml.trim();

    const tokens = tokenize(xml);
    const stack: XmlElement[] = [];
    let root: XmlElement | null = null;

    while (tokens.length > 0) {
        const token = tokens.shift()!;

        if (token.type === 'open-tag') {
            const el = new XmlElement(token.tag, token.attr);

            // 必须存在唯一id属性
            if (!el.attr.id) {
                throw `<${el.tag}> 缺少id属性`;
            }
            if (idTable.has(el.attr.id)) {
                throw `"${el.attr.id}" id不唯一`;
            }
            idTable.set(el.attr.id, el);

            if (stack.length > 0) {
                const parent = stack[stack.length - 1];
                parent.children.push(el);
                el.parent = parent;
            } else if (root) {
                throw "存在多个根元素";
            } else {
                root = el;
            }

            stack.push(el);
        }

        else if (token.type === 'close-tag') {
            const top = stack.pop();
            if (!top || top.tag !== token.tag) {
                throw `</${token.tag}> 闭合标签不匹配`;
            }

            // 不能同时有文本和子节点
            if (top.text && top.children.length > 0) {
                throw `节点 <${top.tag}> 不能同时有文本和子节点`;
            }

        }

        else if (token.type === 'text') {
            const top = stack[stack.length - 1];
            if (!top) {
                if (!token.text.trim()) continue;
                throw "根元素外发现文本";
            }

            const text = token.text.trim();
            if (text.length === 0) continue;

            if (top.children.length > 0) {
                throw `节点 <${top.tag}> 不能同时有文本和子节点`;
            }

            top.text = (top.text ?? "") + text;
        }
    }

    if (!root) {
        throw "根节点不存在";
    }
    if (stack.length > 0) {
        throw "存在未闭合的标签";
    }

    return root;
}



type XmlVisual = XmlElement | string;

class XmlElementProvider implements NameProvider<XmlVisual> {
    getName(node: XmlVisual): string {
        if (typeof node === 'string') {
            return `"${node}"`;
        }

        let attrs = `id="${node.attr.id}"`;
        for (const [key, value] of Object.entries(node.attr)) {
            if (key === 'id') continue;
            attrs += `,${key}="${value}"`;
        }
        return `${node.tag} [${attrs}]`;
}
}

class XmlTreeProvider implements TreeContentProvider<XmlVisual> {
    private root: XmlElement;

    constructor(root: XmlElement) {
        this.root = root;
    }

    getRoots(): XmlVisual[] {
        return [this.root];
    }

    getChildren(parent: XmlVisual): XmlVisual[] {
        if (typeof parent === 'string') {
            return [];
        }

        const children: XmlVisual[] = [];
        if (parent.text) children.push(parent.text);
        children.push(...parent.children);
        return children;
    }
}



export class XmlElement {
    constructor(
        public tag: string,
        public attr: Record<string, string> = {},
        public text: string | null = null,
        public parent: XmlElement | null = null,
        public children: XmlElement[] = []
    ) {};

    private getIndent(): string {
        let indent = '';
        let current = this.parent;
        while (current) {
            indent += '    ';
            current = current.parent;
        }
        return indent;
    }

    toString(): string {
        const indent = this.getIndent();
        let attrs = '';
        for (const [key, value] of Object.entries(this.attr)) {
            attrs += ` ${key}="${value}"`;
        }
        let result = `${indent}<${this.tag}${attrs}>`;
        if (this.text) {
            result += this.text;
        }
        if (this.children.length > 0) {
            result += '\n' + this.children.map(child => child.toString()).join('\n') + `\n${indent}`;
        } else if (!this.parent){
            result += `\n${indent}`;  // 根元素无论如何都换行
        }
        result += `</${this.tag}>`;
        return result;
    }
}


export class XmlEditor extends Editor {
    private root: XmlElement = null!;
    private idTable: Map<string, XmlElement> = new Map();

    static create(path: string, eventBus: EventBus): XmlEditor {
        const fileExists = fs.existsSync(path);
        const info: EditorInfo = {
            type: 'xml',
            id: randomUUID(),
            path,
            modified: !fileExists,
            loggingEnabled: false
        }
        const editor = new XmlEditor(info, eventBus);

        if (fileExists) {
            let content = fs.readFileSync(path, 'utf-8');
            if (!content.startsWith(XML_DECLARATION)) {
                throw "不是有效的XML文件";
            }
            content = content.slice(XML_DECLARATION.length);
            editor.root = parseXML(content, editor.idTable);
            if (editor.root.attr.log === 'true') {
                editor.info.loggingEnabled = true;
            }
        }
        else {
            editor.root = new XmlElement('root', { id: 'root' });
            editor.idTable.set('root', editor.root);
        }

        return editor;
    }

    getContent(): string {
        return XML_DECLARATION + this.root.toString();
    }

    insertBefore(tag: string, newId: string, targetId: string, text: string | null = null): void {
        const target = this.idTable.get(targetId);
        if (!target) throw `目标元素不存在: ${targetId}`;
        if (this.idTable.has(newId)) throw `元素ID已存在: ${newId}`;
        if (!target.parent) throw `不能在根元素前插入元素`;

        const newElement = new XmlElement(tag, { id: newId }, text, target.parent);
        const index = target.parent.children.indexOf(target);
        target.parent.children.splice(index, 0, newElement);
        this.idTable.set(newId, newElement);
    }

    delete(id: string): {element: XmlElement, parentId: string, index: number} {
        const element = this.idTable.get(id);
        if (!element) throw `元素不存在: ${id}`;
        if (!element.parent) throw `不能删除根元素`;

        const index = element.parent.children.indexOf(element);
        element.parent.children.splice(index, 1);
        this.idTable.delete(id);
        return { element, parentId: element.parent.attr.id, index };
    }

    appendChild(tag: string, newId: string, parentId: string, text: string | null = null): void {
        const parent = this.idTable.get(parentId);
        if (!parent) throw `父元素不存在: ${parentId}`;
        if (this.idTable.has(newId)) throw `元素ID已存在: ${newId}`;
        if (parent.text) throw `该元素已有⽂本内容，不⽀持混合内容`;

        const newElement = new XmlElement(tag, { id: newId }, text, parent);
        parent.children.push(newElement);
        this.idTable.set(newId, newElement);
    }

    editId(oldId: string, newId: string): void {
        const element = this.idTable.get(oldId);
        if (!element) throw `元素不存在: ${oldId}`;
        if (this.idTable.has(newId)) throw `目标ID已存在: ${newId}`;
        if (!element.parent) throw `不允许修改根元素ID`;

        element.attr.id = newId;
        this.idTable.delete(oldId);
        this.idTable.set(newId, element);
    }
    
    editText(id: string, newText: string | null): string | null {
        const element = this.idTable.get(id);
        if (!element) throw `元素不存在: ${id}`;
        if (newText && element.children.length > 0) throw `该元素有子元素，不支持混合内容`;

        const originalText = element.text;
        element.text = newText;
        return originalText;
    }

    undoDelete(element: XmlElement, parentId: string, index: number): void {
        const parent = this.idTable.get(parentId);
        if (!parent) throw `撤销错误: ${parentId}`;

        element.parent = parent;
        parent.children.splice(index, 0, element);
        this.idTable.set(element.attr.id, element);
    }

    showTree(): void {
        const contentProvider = new XmlTreeProvider(this.root);
        const nameProvider = new XmlElementProvider();
        const viewer = new VisualTreeViewer<XmlVisual>(contentProvider, nameProvider);
        viewer.show();
    }


    doSpellCheck(): void {
        type Suggestion = { id: string, word: string, suggestions: string[] };
        const suggestions: Suggestion[] = [];
        function traverse(node: XmlElement) {
            if (node.text) {
                const words = node.text.split(/\W+/);
                for (const word of words) {
                    if (word.length === 0) continue;
                    const result = checkSpell(word);
                    if (result) {
                        suggestions.push({ id: node.attr.id, word, suggestions: result });
                    }
                }
            }
            for (const child of node.children) {
                traverse(child);
            }
        }
        traverse(this.root);

        let report = `拼写检查结果:\n`;
        if (suggestions.length === 0) report += `未发现拼写错误。`;
        for (const s of suggestions) {
            report += `元素 ${s.id}: "${s.word}" -> `;
            report += s.suggestions.length > 0 ? `建议: ${s.suggestions.join(', ')}` : `找不到建议`;
            report += `\n`;
        }
        if (report.endsWith('\n')) report = report.slice(0, -1);
        console.log(report);
    }
    
    logInit(): void {
        this.root.attr.log = 'true';
        this.info.loggingEnabled = true;
    }
}

