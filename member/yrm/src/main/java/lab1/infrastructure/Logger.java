package lab1.infrastructure;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// 不再是一个纯静态类
public class Logger {
    private static Logger instance;
    private final IFileSystem fileSystem; // 2. 依赖注入

    // 移到 LoggingService 去，Logger 只负责 I/O
    // private static final DateTimeFormatter formatter =
    //    DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

    // 私有构造函数
    private Logger(IFileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    // 公共的 getInstance，用于生产
    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger(new FileSystem());
        }
        return instance;
    }

    // (用于测试) 允许注入 Mock
    public static Logger getTestInstance(IFileSystem fileSystem) {
        instance = new Logger(fileSystem);
        return instance;
    }

    // 方法改为非静态
    public void writeLog(String logFilePath, String message) {
        try (FileWriter fw = new FileWriter(logFilePath, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(message);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("日志写入失败: " + e.getMessage());
        }
    }

    // 移到 LoggingService 去
    // public static String formatTimestamp(LocalDateTime time) { ... }

    // 方法改为非静态
    public String readLog(String logFilePath) throws IOException {
        // 9. 使用注入的 fileSystem 实例
        if (!fileSystem.fileExists(logFilePath)) {
            return "日志文件不存在";
        }
        // 9. 使用注入的 fileSystem 实例
        return fileSystem.readFile(logFilePath);
    }
}