package com.editor.core.workspace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作区状态备忘录（Memento模式）
 * 用于保存和恢复工作区状态
 */
public class WorkspaceMemento {
    private List<String> openFiles;
    private String activeFile;
    private Map<String, Boolean> modifiedStatus;
    private Map<String, Boolean> loggingEnabled;
    
    public WorkspaceMemento(
            List<String> openFiles,
            String activeFile,
            Map<String, Boolean> modifiedStatus,
            Map<String, Boolean> loggingEnabled) {
        this.openFiles = openFiles != null ? new java.util.ArrayList<>(openFiles) : new java.util.ArrayList<>();
        this.activeFile = activeFile;
        this.modifiedStatus = modifiedStatus != null ? new HashMap<>(modifiedStatus) : new HashMap<>();
        this.loggingEnabled = loggingEnabled != null ? new HashMap<>(loggingEnabled) : new HashMap<>();
    }
    
    public List<String> getOpenFiles() {
        return new java.util.ArrayList<>(openFiles);
    }
    
    public String getActiveFile() {
        return activeFile;
    }
    
    public Map<String, Boolean> getModifiedStatus() {
        return new HashMap<>(modifiedStatus);
    }
    
    public Map<String, Boolean> getLoggingEnabled() {
        return new HashMap<>(loggingEnabled);
    }
}
