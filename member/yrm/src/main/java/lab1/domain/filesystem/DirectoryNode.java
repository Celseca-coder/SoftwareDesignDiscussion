package lab1.domain.filesystem;

import java.util.*;

public class DirectoryNode implements FileSystemNode {
    private String name;
    private List<FileSystemNode> children;

    public DirectoryNode(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }

    public void addChild(FileSystemNode node) {
        children.add(node);
    }

    public List<FileSystemNode> getChildren() {
        return children;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public void accept(TreeVisitor visitor, String prefix, boolean isLast) {
        visitor.visitDirectory(this, prefix, isLast);
    }
}
