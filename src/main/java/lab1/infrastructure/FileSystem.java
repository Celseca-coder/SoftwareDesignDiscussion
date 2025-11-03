package lab1.infrastructure;

// ==================== 基础设施层 (Infrastructure Layer) ====================

// FileSystem.java

import lab1.domain.filesystem.*;
import lab1.domain.filesystem.*; // <-- 这一行很可能丢失了，请添加它
import java.nio.file.*;
import java.io.*;
import java.util.*;

public class FileSystem {
    public static String readFile(String path) throws IOException {
        return Files.readString(Paths.get(path));
    }

    public static void writeFile(String path, String content) throws IOException {
        Files.writeString(Paths.get(path), content);
    }

    public static boolean fileExists(String path) {
        return Files.exists(Paths.get(path));
    }

    public static FileSystemNode buildTree(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("路径不存在: " + path);
        }
        return buildTreeRecursive(file);
    }

    private static FileSystemNode buildTreeRecursive(File file) {
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