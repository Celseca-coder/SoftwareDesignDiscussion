package editor.core;

import editor.observer.EventPublisher;

import java.util.List;

/**
 * 编辑器基类接口
 */
public abstract class Editor {
    protected String filePath;
    protected boolean modified;

    public Editor(String filePath) {
        this.filePath = filePath;
        this.modified = false;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    // 抽象方法,由子类实现
    public abstract void load() throws Exception;
    public abstract void save() throws Exception;
    public abstract void show();
    public abstract void show(int startLine, int endLine);
    public abstract void undo();
    public abstract void redo();
    public abstract List<String> getLines();
    public abstract void enableLogging(EventPublisher publisher);
}
