package com.editor.core.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件持久化服务
 * 负责文件的保存和加载（UTF-8编码）
 */
public class FilePersistence {
    
    /**
     * 读取文件内容
     * @param filePath 文件路径
     * @return 文本行列表
     * @throws IOException 如果文件读取失败
     */
    public List<String> loadFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }
    
    /**
     * 保存文件内容
     * @param filePath 文件路径
     * @param lines 文本行列表
     * @throws IOException 如果文件保存失败
     */
    public void saveFile(String filePath, List<String> lines) throws IOException {
        Path path = Paths.get(filePath);
        
        // 确保父目录存在
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        
        // 写入文件（UTF-8编码）
        Files.write(path, lines, StandardCharsets.UTF_8);
    }
    
    /**
     * 检查文件是否存在
     * @param filePath 文件路径
     * @return true表示文件存在
     */
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * 检查文件首行是否为 "# log"
     * @param filePath 文件路径
     * @return true表示首行是 "# log"
     */
    public boolean isLogEnabled(String filePath) {
        try {
            List<String> lines = loadFile(filePath);
            return !lines.isEmpty() && lines.get(0).trim().equals("# log");
        } catch (IOException e) {
            return false;
        }
    }
}
