package com.editor.core.workspace;

import com.editor.core.editor.Editor;
import com.editor.core.logging.EditorEvent;
import com.editor.core.logging.EventListener;
import com.editor.core.logging.LoggingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作区类
 * 管理当前会话的全局状态，协调各个模块
 */
public class Workspace {
    private EditorManager editorManager;         // 编辑器管理器，管理所有已打开的文件
    private String activeFile;                   // 活动文件路径
    private Map<String, Boolean> modifiedStatus; // 文件路径 -> 是否已修改
    private Map<String, Boolean> loggingEnabled; // 文件路径 -> 是否启用日志
    private List<EventListener> eventListeners; // 事件监听器列表（观察者模式）
    private LoggingService loggingService;
    private java.util.LinkedList<String> fileAccessOrder; // 文件访问顺序（最近使用的在前）
    
    public Workspace(LoggingService loggingService) {
        this.editorManager = new EditorManager();
        this.activeFile = null;
        this.modifiedStatus = new HashMap<>();
        this.loggingEnabled = new HashMap<>();
        this.eventListeners = new ArrayList<>();
        this.loggingService = loggingService;
        this.fileAccessOrder = new java.util.LinkedList<>();
        
        // 注册日志服务为观察者
        addListener(loggingService);
    }
    
    /**
     * 添加事件监听器（观察者模式）
     */
    public void addListener(EventListener listener) {
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }
    
    /**
     * 移除事件监听器
     */
    public void removeListener(EventListener listener) {
        eventListeners.remove(listener);
    }
    
    /**
     * 通知所有监听器（观察者模式）
     */
    private void notifyListeners(EditorEvent event) {
        for (EventListener listener : eventListeners) {
            listener.onEvent(event);
        }
    }
    
    /**
     * 打开文件（添加编辑器）
     */
    public void openFile(String filePath, Editor editor) {
        editorManager.addEditor(filePath, editor);
        updateModifiedStatus(filePath, editor.isModified());
        
        // 更新文件访问顺序
        fileAccessOrder.remove(filePath);
        fileAccessOrder.addFirst(filePath);
        
        // 检查是否需要启用日志
        loggingService.initializeLoggingIfNeeded(filePath);
        
        // 发布文件打开事件
        EditorEvent event = new EditorEvent(
            EditorEvent.EventType.FILE_OPENED,
            filePath,
            "load",
            filePath
        );
        notifyListeners(event);
        
        // 如果没有活动文件，设置为活动文件
        if (activeFile == null) {
            setActiveFile(filePath);
        }
    }
    
    /**
     * 关闭文件（移除编辑器）
     */
    public void closeFile(String filePath) {
        if (!editorManager.hasEditor(filePath)) {
            throw new IllegalStateException("文件未打开: " + filePath);
        }
        
        editorManager.removeEditor(filePath);
        modifiedStatus.remove(filePath);
        loggingEnabled.remove(filePath);
        fileAccessOrder.remove(filePath);
        
        // 如果关闭的是活动文件，切换到最近使用的文件
        if (filePath.equals(activeFile)) {
            String recentlyUsedFile = getRecentlyUsedFile(filePath);
            if (recentlyUsedFile != null) {
                setActiveFile(recentlyUsedFile);
            } else {
                activeFile = null;
            }
        }
        
        // 发布文件关闭事件
        EditorEvent event = new EditorEvent(
            EditorEvent.EventType.FILE_CLOSED,
            filePath,
            "close",
            filePath
        );
        notifyListeners(event);
    }
    
    /**
     * 设置活动文件
     */
    public void setActiveFile(String filePath) {
        if (filePath != null && !editorManager.hasEditor(filePath)) {
            throw new IllegalStateException("文件未打开: " + filePath);
        }
        this.activeFile = filePath;
        
        // 更新文件访问顺序（最近使用的移到最前）
        if (filePath != null) {
            fileAccessOrder.remove(filePath);
            fileAccessOrder.addFirst(filePath);
        }
    }
    
    /**
     * 获取最近使用的文件（排除指定文件）
     * @param excludeFile 要排除的文件路径
     * @return 最近使用的文件路径，如果没有则返回null
     */
    public String getRecentlyUsedFile(String excludeFile) {
        for (String filePath : fileAccessOrder) {
            if (!filePath.equals(excludeFile) && editorManager.hasEditor(filePath)) {
                return filePath;
            }
        }
        return null;
    }
    
    /**
     * 获取活动文件路径
     */
    public String getActiveFile() {
        return activeFile;
    }
    
    /**
     * 获取活动编辑器
     */
    public Editor getActiveEditor() {
        if (activeFile == null) {
            throw new IllegalStateException("没有活动文件");
        }
        return editorManager.getEditor(activeFile);
    }
    
    /**
     * 获取指定文件的编辑器
     */
    public Editor getEditor(String filePath) {
        Editor editor = editorManager.getEditor(filePath);
        if (editor == null) {
            throw new IllegalStateException("文件未打开: " + filePath);
        }
        return editor;
    }
    
    /**
     * 检查文件是否已打开
     */
    public boolean isFileOpen(String filePath) {
        return editorManager.hasEditor(filePath);
    }
    
    /**
     * 获取所有打开的文件路径
     */
    public List<String> getOpenFiles() {
        return new ArrayList<>(editorManager.getAllFilePaths());
    }
    
    /**
     * 更新文件修改状态
     */
    public void updateModifiedStatus(String filePath, boolean modified) {
        modifiedStatus.put(filePath, modified);
        
        if (modified) {
            // 发布文件修改事件
            EditorEvent event = new EditorEvent(
                EditorEvent.EventType.FILE_MODIFIED,
                filePath,
                "edit",
                ""
            );
            notifyListeners(event);
        }
    }
    
    /**
     * 获取文件修改状态
     */
    public boolean isFileModified(String filePath) {
        Boolean modified = modifiedStatus.get(filePath);
        return modified != null && modified;
    }
    
    /**
     * 检查是否有未保存的文件
     */
    public boolean hasUnsavedFiles() {
        return modifiedStatus.values().stream().anyMatch(modified -> modified);
    }
    
    /**
     * 获取所有未保存的文件
     */
    public List<String> getUnsavedFiles() {
        List<String> unsaved = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : modifiedStatus.entrySet()) {
            if (entry.getValue()) {
                unsaved.add(entry.getKey());
            }
        }
        return unsaved;
    }
    
    /**
     * 启用文件的日志记录
     */
    public void enableLogging(String filePath) {
        if (!editorManager.hasEditor(filePath)) {
            throw new IllegalStateException("文件未打开: " + filePath);
        }
        loggingEnabled.put(filePath, true);
        loggingService.enableLogging(filePath);
    }
    
    /**
     * 关闭文件的日志记录
     */
    public void disableLogging(String filePath) {
        if (!editorManager.hasEditor(filePath)) {
            throw new IllegalStateException("文件未打开: " + filePath);
        }
        loggingEnabled.put(filePath, false);
        loggingService.disableLogging(filePath);
    }
    
    /**
     * 检查文件是否启用日志
     */
    public boolean isLoggingEnabled(String filePath) {
        return loggingService.isLoggingEnabled(filePath);
    }
    
    /**
     * 保存工作区状态（创建备忘录）
     */
    public WorkspaceMemento saveState() {
        List<String> openFiles = getOpenFiles();
        Map<String, Boolean> modifiedStatus = new HashMap<>(this.modifiedStatus);
        Map<String, Boolean> loggingEnabled = new HashMap<>(this.loggingEnabled);
        
        return new WorkspaceMemento(openFiles, activeFile, modifiedStatus, loggingEnabled);
    }
    
    /**
     * 恢复工作区状态（从备忘录恢复）
     * 注意：这个方法只恢复元数据，不恢复编辑器内容
     */
    public void restoreState(WorkspaceMemento memento) {
        this.activeFile = memento.getActiveFile();
        this.modifiedStatus = memento.getModifiedStatus();
        this.loggingEnabled = memento.getLoggingEnabled();
    }
    
    /**
     * 发布命令执行事件
     */
    public void notifyCommandExecuted(String commandName, String commandArgs, String filePath) {
        EditorEvent event = new EditorEvent(
            EditorEvent.EventType.COMMAND_EXECUTED,
            filePath,
            commandName,
            commandArgs
        );
        notifyListeners(event);
    }
    
    /**
     * 发布文件保存事件
     */
    public void notifyFileSaved(String filePath) {
        EditorEvent event = new EditorEvent(
            EditorEvent.EventType.FILE_SAVED,
            filePath,
            "save",
            filePath
        );
        notifyListeners(event);
        
        // 更新修改状态
        updateModifiedStatus(filePath, false);
    }
    
    /**
     * 获取编辑器管理器
     */
    public EditorManager getEditorManager() {
        return editorManager;
    }
}
