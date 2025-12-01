package lab1.infrastructure;

import lab1.domain.filesystem.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

// 实现接口，并将方法改为非静态
public class FileSystem implements IFileSystem {

    @Override
    public String readFile(String path) throws IOException {
        return Files.readString(Paths.get(path));
    }

    @Override
    public void writeFile(String path, String content) throws IOException {
        Files.writeString(Paths.get(path), content);
    }

    @Override
    public boolean fileExists(String path) {
        return Files.exists(Paths.get(path));
    }

    @Override
    public FileSystemNode buildTree(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("路径不存在: " + path);
        }
        return buildTreeRecursive(file);
    }

    private FileSystemNode buildTreeRecursive(File file) {
        if (file.isDirectory()) {
            DirectoryNode dirNode = new DirectoryNode(file.getName());
            File[] children = file.listFiles();
            if (children != null) {
                Arrays.sort(children, Comparator.comparing(File::getName));
                for (File child : children) {
                    dirNode.addChild(buildTreeRecursive(child));
                }
            }
            return dirNode;
        } else {
            return new FileNode(file.getName());
        }
    }
}