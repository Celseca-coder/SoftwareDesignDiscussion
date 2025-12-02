package lab1.domain.filesystem;

import java.util.List;

public class TreeDisplayVisitor implements TreeVisitor {
    private StringBuilder output;

    public TreeDisplayVisitor() {
        this.output = new StringBuilder();
    }

    @Override
    public void visitFile(FileNode file, String prefix, boolean isLast) {
        output.append(prefix);
        output.append(isLast ? "└── " : "├── ");
        output.append(file.getName());
        output.append("\n");
    }

    @Override
    public void visitDirectory(DirectoryNode dir, String prefix, boolean isLast) {
        output.append(prefix);
        output.append(isLast ? "└── " : "├── ");
        output.append(dir.getName());
        output.append("\n");

        List<FileSystemNode> children = dir.getChildren();
        String childPrefix = prefix + (isLast ? "    " : "│   ");
        for (int i = 0; i < children.size(); i++) {
            children.get(i).accept(this, childPrefix, i == children.size() - 1);
        }
    }

    public String getOutput() {
        return output.toString();
    }
}