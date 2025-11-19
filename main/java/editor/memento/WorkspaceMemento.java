package editor.memento;

import java.io.Serializable;
import java.util.List;

/**
 * 工作区备忘录
 */
public class WorkspaceMemento implements Serializable {
    private List<String> openFiles;
    private String activeFile;

    public List<String> getOpenFiles() {
        return openFiles;
    }

    public void setOpenFiles(List<String> openFiles) {
        this.openFiles = openFiles;
    }

    public String getActiveFile() {
        return activeFile;
    }

    public void setActiveFile(String activeFile) {
        this.activeFile = activeFile;
    }
}
