package lab1.application;

import java.util.*;

public class WorkspaceState {
    private List<String> openFiles;
    private String activeFile;
    private Set<String> modifiedFiles; // 仅用于恢复时设置，实际状态在Editor中
    private Set<String> logEnabledFiles;

    public WorkspaceState() {
        this.openFiles = new ArrayList<>();
        this.modifiedFiles = new HashSet<>();
        this.logEnabledFiles = new HashSet<>();
    }

    public WorkspaceState(List<String> openFiles, String activeFile,
                          Set<String> modifiedFiles, Set<String> logEnabledFiles) {
        this.openFiles = openFiles;
        this.activeFile = activeFile;
        this.modifiedFiles = modifiedFiles;
        this.logEnabledFiles = logEnabledFiles;
    }

    // Getters and setters
    public List<String> getOpenFiles() { return openFiles; }
    public void setOpenFiles(List<String> openFiles) { this.openFiles = openFiles; }

    public String getActiveFile() { return activeFile; }
    public void setActiveFile(String activeFile) { this.activeFile = activeFile; }

    public Set<String> getModifiedFiles() { return modifiedFiles; }
    public void setModifiedFiles(Set<String> modifiedFiles) { this.modifiedFiles = modifiedFiles; }

    public Set<String> getLogEnabledFiles() { return logEnabledFiles; }
    public void setLogEnabledFiles(Set<String> logEnabledFiles) { this.logEnabledFiles = logEnabledFiles; }
}
