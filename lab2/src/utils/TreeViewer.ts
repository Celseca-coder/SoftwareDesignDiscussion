import * as fs from "fs";
import * as path from "path";

export interface NameProvider<T> {
    getName(node: T): string;
}

export interface TreeContentProvider<T> {
    getRoots(): T[];
    getChildren(parent: T): T[];
}


type Visitor<T> = (node: Node<T>) => void;

class Node<T> {
    constructor(public data: T, public hasNextSibling: boolean[]) { }
}


export class VisualTreeViewer<T> {
    constructor(
        private contentProvider: TreeContentProvider<T>,
        private nameProvider: NameProvider<T>
    ) { }

    private visitor(node: Node<T>): void {
        node.hasNextSibling.forEach((hasNext, idx) => {
            const isLast = idx === node.hasNextSibling.length - 1;
            if (isLast) {
                process.stdout.write(hasNext ? "├── " : "└── ");
            } else {
                process.stdout.write(hasNext ? "│   " : "    ");
            }
        });
        process.stdout.write(this.nameProvider.getName(node.data) + "\n");
    }

    private visitIterator(
        iterator: IterableIterator<T>,
        hasNextSibling: boolean[],
    ): void {
        const items = Array.from(iterator);
        items.forEach((node, idx) => {
            const newNext = [...hasNextSibling, idx < items.length - 1];
            this.visitNode(node, newNext);
        });
    }

    private visitNode(node: T, hasNextSibling: boolean[]) {
        this.visitor(new Node(node, hasNextSibling));
        const children = this.contentProvider.getChildren(node);
        this.visitIterator(children.values(), hasNextSibling);
    }

    show(): void {
        const roots = this.contentProvider.getRoots();
        this.visitIterator(roots.values(), []);
    }
}


type File = string;

class FileNameProvider implements NameProvider<File> {
    getName(filePath: File): string {
        return path.basename(filePath);
    }
}

class DirTreeProvider implements TreeContentProvider<File> {
    private root: File;

    constructor(root: File) {
        this.root = root;
    }

    getRoots(): File[] {
        const files = fs.readdirSync(this.root);
        return files.map((f) => path.join(this.root, f));
    }

    getChildren(parent: File): File[] {
        if (fs.statSync(parent).isDirectory()) {
            const children = fs.readdirSync(parent);
            return children.map((c) => path.join(parent, c));
        }
        return [];
    }
}


export function showDirTree(rootPath: string): void {
    const treeProvider = new DirTreeProvider(rootPath);
    const nameProvider = new FileNameProvider();
    const treeViewer = new VisualTreeViewer<File>(treeProvider, nameProvider);
    treeViewer.show();
}
