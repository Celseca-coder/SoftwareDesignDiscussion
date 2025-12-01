package lab1.infrastructure;

import lab1.domain.filesystem.FileSystemNode;
import java.io.IOException;

// 需要一个接口，以便可以 Mock 它
public interface IFileSystem {
    String readFile(String path) throws IOException;
    void writeFile(String path, String content) throws IOException;
    boolean fileExists(String path);
    FileSystemNode buildTree(String path) throws IOException;
}
