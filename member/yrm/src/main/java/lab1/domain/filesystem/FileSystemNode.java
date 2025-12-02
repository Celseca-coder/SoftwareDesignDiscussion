package lab1.domain.filesystem;

public interface FileSystemNode {
    String getName();
    boolean isDirectory();
    void accept(TreeVisitor visitor, String prefix, boolean isLast);
}