package editor.workspace;

import editor.core.Editor;
import editor.observer.EventPublisher;
import editor.memento.WorkspaceMemento;

import java.io.*;
import java.util.*;

/**
 * 工作区管理器
 */
public class Workspace {
    private Map<String, Editor> editors;
    private Editor activeEditor;
    private EventPublisher eventPublisher;
    private Stack<Editor> recentEditors; // 最近使用的文件栈
    private static final String WORKSPACE_STATE_FILE = ".workspace.state";

    public Workspace() {
        this.editors = new LinkedHashMap<>();
        this.eventPublisher = new EventPublisher();
        this.recentEditors = new Stack<>();
    }

    public EventPublisher getEventPublisher() {
        return eventPublisher;
    }

    public void addEditor(String filePath, Editor editor) {
        editors.put(filePath, editor);
        setActiveEditorWithHistory(editor);
    }

    public Editor getEditor(String filePath) {
        return editors.get(filePath);
    }

    public Editor getActiveEditor() {
        return activeEditor;
    }

    public void setActiveEditor(Editor editor) {
        setActiveEditorWithHistory(editor);
    }

    /**
     * 设置活动编辑器并记录到历史栈中
     */
    private void setActiveEditorWithHistory(Editor editor) {
        if (activeEditor != null && activeEditor != editor) {
            // 从栈中移除已存在的条目(如果有)
            recentEditors.remove(activeEditor);
            // 将当前活动编辑器压入栈
            recentEditors.push(activeEditor);
        }
        this.activeEditor = editor;
    }

    public Collection<Editor> getAllEditors() {
        return editors.values();
    }

    public void removeEditor(String filePath) {
        Editor editor = editors.remove(filePath);
        if (editor == activeEditor) {
            // 从历史栈中移除该编辑器
            recentEditors.remove(editor);
            
            // 切换到最近使用的文件
            activeEditor = null;
            while (!recentEditors.isEmpty()) {
                Editor recent = recentEditors.pop();
                if (editors.containsValue(recent)) {
                    activeEditor = recent;
                    break;
                }
            }
            
            // 如果没有历史记录,切换到第一个打开的文件
            if (activeEditor == null && !editors.isEmpty()) {
                activeEditor = editors.values().iterator().next();
            }
        } else {
            // 从历史栈中移除该编辑器
            recentEditors.remove(editor);
        }
    }

    /**
     * 保存工作区状态
     */
    public void saveWorkspaceState() {
        try {
            WorkspaceMemento memento = new WorkspaceMemento();
            memento.setOpenFiles(new ArrayList<>(editors.keySet()));
            if (activeEditor != null) {
                memento.setActiveFile(activeEditor.getFilePath());
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(WORKSPACE_STATE_FILE))) {
                oos.writeObject(memento);
            }
        } catch (Exception e) {
            System.err.println("Failed to save workspace state: " + e.getMessage());
        }
    }

    /**
     * 加载工作区状态
     */
    public void loadWorkspaceState() {
        try {
            File stateFile = new File(WORKSPACE_STATE_FILE);
            if (!stateFile.exists()) {
                return;
            }

            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(WORKSPACE_STATE_FILE))) {
                WorkspaceMemento memento = (WorkspaceMemento) ois.readObject();
                
                // 恢复打开的文件
                for (String filePath : memento.getOpenFiles()) {
                    try {
                        File file = new File(filePath);
                        if (file.exists()) {
                            editor.core.TextEditor editor = new editor.core.TextEditor(filePath);
                            editor.load();
                            editors.put(filePath, editor);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to restore file: " + filePath);
                    }
                }

                // 恢复活动文件
                if (memento.getActiveFile() != null) {
                    activeEditor = editors.get(memento.getActiveFile());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load workspace state: " + e.getMessage());
        }
    }
}
