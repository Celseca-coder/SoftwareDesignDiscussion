package com.editor.core;

import com.editor.pattern.observer.Event;
import com.editor.pattern.observer.Subject;
import com.editor.pattern.memento.WorkspaceMemento;
import com.editor.pattern.memento.Originator;
import com.editor.exception.FileNotOpenException;

import java.util.ArrayList;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;

/**
 * 工作区
 * 管理多个编辑器实例
 */
public class Workspace extends Subject implements Originator<WorkspaceMemento> {
    private List<Editor> editors;
    private Editor activeEditor;
    private Deque<String> recentFiles;
    private static final String STATE_FILE = ".editor_workspace";
    
    public Workspace() {
        this.editors = new ArrayList<>();
        this.recentFiles = new ArrayDeque<>();
    }
    
    /**
     * 加载文件
     */
    public Editor loadFile(String filepath) throws Exception {
        // 检查是否已打开
        for (Editor editor : editors) {
            if (editor.getFilepath().equals(filepath)) {
                activeEditor = editor;
                updateRecentFiles(filepath);
                return editor;
            }
        }
        
        // 创建新编辑器
        Editor editor = new TextEditor(filepath);
        editor.load();
        editors.add(editor);
        activeEditor = editor;
        updateRecentFiles(filepath);
        
        notifyObservers(new Event("file_loaded", filepath));
        return editor;
    }
    
    /**
     * 关闭文件
     */
    public void closeFile(String filepath) throws Exception {
        Editor targetEditor = null;
        for (Editor editor : editors) {
            if (editor.getFilepath().equals(filepath)) {
                targetEditor = editor;
                break;
            }
        }
        
        if (targetEditor != null) {
            editors.remove(targetEditor);
            if (activeEditor == targetEditor) {
                if (editors.isEmpty()) {
                    activeEditor = null;
                } else {
                    activeEditor = editors.get(0);
                }
            }
            notifyObservers(new Event("file_closed", filepath));
        }
    }
    
    /**
     * 切换活动编辑器
     */
    public void setActiveEditor(String filepath) {
        for (Editor editor : editors) {
            if (editor.getFilepath().equals(filepath)) {
                activeEditor = editor;
                break;
            }
        }
    }
    
    /**
     * 更新最近文件列表
     */
    private void updateRecentFiles(String filepath) {
        recentFiles.remove(filepath);
        recentFiles.addFirst(filepath);
        // 限制最近文件数量
        while (recentFiles.size() > 10) {
            recentFiles.removeLast();
        }
    }
    
    @Override
    public WorkspaceMemento createMemento() {
        List<String> openFiles = new ArrayList<>();
        for (Editor editor : editors) {
            openFiles.add(editor.getFilepath());
        }
        String activeFile = activeEditor != null ? activeEditor.getFilepath() : null;
        return new WorkspaceMemento(openFiles, activeFile, recentFiles);
    }
    
    @Override
    public void restoreFromMemento(WorkspaceMemento memento) {
        // 清空当前状态
        editors.clear();
        activeEditor = null;
        
        // 恢复最近文件列表
        recentFiles = new ArrayDeque<>(memento.getRecentFiles());
        
        // 恢复打开的文件
        // 注意：这里只恢复文件列表，不自动加载内容
        // 实际加载需要调用loadFile方法
        notifyObservers(new Event("workspace_restored"));
    }
    
    // Getters
    public Editor getActiveEditor() throws FileNotOpenException {
        if (activeEditor == null) {
            throw new FileNotOpenException("没有活动编辑器");
        }
        return activeEditor;
    }
    
    public List<Editor> getEditors() {
        return new ArrayList<>(editors);
    }
    
    public Deque<String> getRecentFiles() {
        return new ArrayDeque<>(recentFiles);
    }
    
    public boolean hasActiveEditor() {
        return activeEditor != null;
    }
}

