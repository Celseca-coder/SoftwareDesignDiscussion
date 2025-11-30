package lab1.domain.filesystem;


public interface TreeVisitor {
    void visitFile(FileNode file, String prefix, boolean isLast);
    void visitDirectory(DirectoryNode dir, String prefix, boolean isLast);
}
