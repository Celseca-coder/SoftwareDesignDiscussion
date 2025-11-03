package lab1.application;

import lab1.application.event.*;
import lab1.domain.command.*;
import lab1.domain.editor.*;
import lab1.domain.display.*;
import lab1.domain.filesystem.*;
import lab1.infrastructure.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Workspace {
    private static Workspace instance;
    private Map<String, IEditor> editors;
    private IEditor activeEditor;
    private LoggingService loggingService;
    private List<String> accessOrder; // 用于 close 后切换到 "最近使用"

    private Workspace() {
        // 使用 LinkedHashMap 保持插入顺序，便于 editor-list 显示
        this.editors = new LinkedHashMap<>();
        this.loggingService = new LoggingService();
        this.accessOrder = new ArrayList<>();
    }

    public static Workspace getInstance() {
        if (instance == null) {
            instance = new Workspace();
        }
        return instance;
    }

    // *** 文件操作 ***

    public void loadFile(String filePath) throws IOException {
        if (editors.containsKey(filePath)) {
            switchActiveEditor(filePath);
            return;
        }

        String content = "";
        boolean isNewFile = !FileSystem.fileExists(filePath);

        if (!isNewFile) {
            content = FileSystem.readFile(filePath);
        }

        IEditor editor = EditorFactory.createEditor(filePath, content);
        if (isNewFile) {
            editor.setModified(true); // 新文件标记为已修改
        }

        editors.put(filePath, editor);
        switchActiveEditor(filePath);

        // 检查是否需要自动启用日志
        if (content.startsWith("# log")) {
            loggingService.enableLogging(filePath);
            writeSessionStart(filePath);
        }

        EventBus.getInstance().publish(new CommandExecutedEvent(filePath, "load " + filePath));
    }

    public void saveFile(String filePath) throws IOException {
        IEditor editor = editors.get(filePath);
        if (editor == null) {
            throw new IllegalArgumentException("文件未打开: " + filePath);
        }

        FileSystem.writeFile(filePath, editor.getContent());
        editor.setModified(false);

        EventBus.getInstance().publish(new CommandExecutedEvent(filePath, "save " + filePath));
    }

    public void saveAll() throws IOException {
        for (Map.Entry<String, IEditor> entry : editors.entrySet()) {
            if (entry.getValue().isModified()) {
                saveFile(entry.getKey());
            }
        }
    }

    public void initFile(String filePath, boolean withLog) throws IOException {
        if (editors.containsKey(filePath) || FileSystem.fileExists(filePath)) {
            throw new IllegalArgumentException("文件已存在: " + filePath);
        }

        String initialContent = withLog ? "# log" : "";
        IEditor editor = EditorFactory.createEditor(filePath, initialContent);
        editor.setModified(true); // 新缓冲区标记为已修改

        editors.put(filePath, editor);
        switchActiveEditor(filePath);

        if (withLog) {
            loggingService.enableLogging(filePath);
            writeSessionStart(filePath);
        }

        EventBus.getInstance().publish(new CommandExecutedEvent(filePath, "init " + filePath +
                (withLog ? " with-log" : "")));
    }

    public boolean closeFile(String filePath) {
        IEditor editor = editors.get(filePath);
        if (editor == null) {
            throw new IllegalArgumentException("文件未打开: " + filePath);
        }

        if (editor.isModified()) {
            return false; // 需要用户确认
        }

        closeFileForce(filePath);
        return true;
    }

    public void closeFileForce(String filePath) {
        editors.remove(filePath);
        accessOrder.remove(filePath);

        if (activeEditor != null && activeEditor.getFilePath().equals(filePath)) {
            if (!accessOrder.isEmpty()) {
                // 切换到 "最近使用" 的文件
                switchActiveEditor(accessOrder.get(accessOrder.size() - 1));
            } else {
                activeEditor = null;
            }
        }

        EventBus.getInstance().publish(new CommandExecutedEvent(filePath, "close " + filePath));
    }

    // *** 编辑操作 ***

    /** 'edit' 命令的 public 方法 */
    public void switchActiveEditor(String filePath) {
        IEditor editor = editors.get(filePath);
        if (editor == null) {
            throw new IllegalArgumentException("文件未打开: " + filePath);
        }
        this.activeEditor = editor;

        // 管理 accessOrder 以实现 "最近使用"
        accessOrder.remove(filePath);
        accessOrder.add(filePath);
    }

    public void executeEditCommand(ICommand command) {
        if (activeEditor == null) {
            throw new IllegalStateException("没有活动文件");
        }
        activeEditor.executeCommand(command);
        EventBus.getInstance().publish(
                new CommandExecutedEvent(activeEditor.getFilePath(), command.getDescription()));
    }

    public void undo() {
        if (activeEditor == null) {
            throw new IllegalStateException("没有活动文件");
        }
        activeEditor.undo();
        EventBus.getInstance().publish(new CommandExecutedEvent(activeEditor.getFilePath(), "undo"));
    }

    public void redo() {
        if (activeEditor == null) {
            throw new IllegalStateException("没有活动文件");
        }
        activeEditor.redo();
        EventBus.getInstance().publish(new CommandExecutedEvent(activeEditor.getFilePath(), "redo"));
    }

    public String showContent(int startLine, int endLine) {
        if (activeEditor == null) {
            throw new IllegalStateException("没有活动文件");
        }
        List<String> lines = activeEditor.getLines();

        // 确保范围有效
        int actualStart = Math.max(1, startLine);
        int actualEnd = Math.min(lines.size(), endLine);

        // 处理完全空的文件或无效范围
        if (lines.isEmpty() || lines.get(0).isEmpty() || actualStart > actualEnd) {
            if (startLine == 1 && endLine == 1) return "1: "; // 特殊情况：显示空文件的第一行
            return ""; //
        }

        IContentDisplayer displayer = new LineNumberDecorator(new BasicContentDisplayer());
        return displayer.display(lines, actualStart, actualEnd);
    }

    // --- 辅助和新增的 Public 方法 ---

    private void writeSessionStart(String filePath) {
        String logFile = loggingService.getLogFilePath(filePath);
        String timestamp = Logger.formatTimestamp(LocalDateTime.now());
        String logEntry = "session start at " + timestamp;
        Logger.writeLog(logFile, logEntry);
    }

    public IEditor getActiveEditor() {
        return activeEditor;
    }

    public Map<String, IEditor> getOpenEditors() {
        return editors;
    }

    public LoggingService getLoggingService() {
        return loggingService;
    }

    public List<IEditor> getUnsavedEditors() {
        return editors.values().stream()
                .filter(IEditor::isModified)
                .collect(Collectors.toList());
    }

    // --- 持久化方法 (Memento Pattern) ---

    /**
     * 创建工作区状态快照
     */
    public WorkspaceState createMemento() {
        List<String> openFiles = new ArrayList<>(editors.keySet());
        String active = (activeEditor != null) ? activeEditor.getFilePath() : null;
        Set<String> modified = getUnsavedEditors().stream()
                .map(IEditor::getFilePath)
                .collect(Collectors.toSet());
        Set<String> logging = loggingService.getEnabledFiles();

        return new WorkspaceState(openFiles, active, modified, logging);
    }

    /**
     * 从快照恢复工作区状态
     */
    public void restoreFromMemento(WorkspaceState state) throws IOException {
        this.editors.clear();
        this.accessOrder.clear();
        this.activeEditor = null;

        // 恢复日志状态
        this.loggingService.setEnabledFiles(state.getLogEnabledFiles());

        // 重新加载所有文件
        for (String filePath : state.getOpenFiles()) {
            // 使用 try-catch 保证一个文件加载失败不影响其他文件
            try {
                loadFile(filePath); // loadFile 会自动处理 accessOrder
                // 恢复修改状态
                if (state.getModifiedFiles().contains(filePath)) {
                    if (this.editors.containsKey(filePath)) {
                        this.editors.get(filePath).setModified(true);
                    }
                }
            } catch (Exception e) {
                System.err.println("恢复文件失败 " + filePath + ": " + e.getMessage());
            }
        }

        // 恢复活动文件
        if (state.getActiveFile() != null && editors.containsKey(state.getActiveFile())) {
            switchActiveEditor(state.getActiveFile());
        } else if (!this.accessOrder.isEmpty()) {
            // 如果保存的活动文件无效了，就激活列表中的最后一个
            switchActiveEditor(this.accessOrder.get(this.accessOrder.size() - 1));
        }
    }
}