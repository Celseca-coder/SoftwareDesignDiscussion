package lab1.domain.filesystem;

public class FileNode implements FileSystemNode {
    private String name;

    public FileNode(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public void accept(TreeVisitor visitor, String prefix, boolean isLast) {
        visitor.visitFile(this, prefix, isLast);
    }
}
