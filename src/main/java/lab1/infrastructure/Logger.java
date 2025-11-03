package lab1.infrastructure;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

    public static void writeLog(String logFilePath, String message) {
        try (FileWriter fw = new FileWriter(logFilePath, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(message);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("日志写入失败: " + e.getMessage());
        }
    }

    public static String formatTimestamp(LocalDateTime time) {
        return time.format(formatter);
    }

    public static String readLog(String logFilePath) throws IOException {
        if (!FileSystem.fileExists(logFilePath)) {
            return "日志文件不存在";
        }
        return FileSystem.readFile(logFilePath);
    }
}