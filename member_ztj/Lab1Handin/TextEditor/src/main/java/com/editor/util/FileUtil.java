package com.editor.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 文件操作工具类
 */
public class FileUtil {
    
    /**
     * 检查文件是否存在
     */
    public static boolean exists(String filepath) {
        return Files.exists(Paths.get(filepath));
    }
    
    /**
     * 获取文件名
     */
    public static String getFileName(String filepath) {
        return Paths.get(filepath).getFileName().toString();
    }
    
    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String filepath) {
        String fileName = getFileName(filepath);
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }
    
    /**
     * 读取文件内容
     */
    public static String readFile(String filepath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filepath)));
    }
    
    /**
     * 写入文件内容
     */
    public static void writeFile(String filepath, String content) throws IOException {
        Files.write(Paths.get(filepath), content.getBytes());
    }
    
    /**
     * 创建目录（如果不存在）
     */
    public static void createDirectories(String dirpath) throws IOException {
        Files.createDirectories(Paths.get(dirpath));
    }
}

