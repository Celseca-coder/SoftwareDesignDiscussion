// LogObserver.java
package editor.observer;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 日志观察者
 */
public class LogObserver implements Observer {
    private String filePath;
    private String logFilePath;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    public LogObserver(String filePath) {
        this.filePath = filePath;
        this.logFilePath = getLogFilePath(filePath);
    }

    private String getLogFilePath(String filePath) {
        File file = new File(filePath);
        String parent = file.getParent();
        String name = file.getName();
        String logName = "." + name + ".log";
        return parent != null ? parent + File.separator + logName : logName;
    }

    public void logSessionStart() {
        try {
            String timestamp = dateFormat.format(new Date());
            appendToLog("session start at " + timestamp);
        } catch (IOException e) {
            System.err.println("Warning: Failed to log session start: " + e.getMessage());
        }
    }

    @Override
    public void update(WorkspaceEvent event) {
        // 只记录与当前文件相关的命令事件
        if (event.getType().equals("COMMAND_EXECUTED")) {
            try {
                String timestamp = dateFormat.format(new Date(event.getTimestamp()));
                appendToLog(timestamp + " " + event.getData());
            } catch (IOException e) {
                System.err.println("Warning: Failed to write log: " + e.getMessage());
            }
        }
    }

    private void appendToLog(String content) throws IOException {
        Files.write(Paths.get(logFilePath),
                (content + "\n").getBytes("UTF-8"),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    public void showLog() {
        try {
            File logFile = new File(logFilePath);
            if (!logFile.exists()) {
                System.out.println("未找到日志文件");
                return;
            }

            List<String> lines = Files.readAllLines(logFile.toPath());
            if (lines.isEmpty()) {
                System.out.println("日志文件为空");
            } else {
                for (String line : lines) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            System.err.println("读取日志失败: " + e.getMessage());
        }
    }
}