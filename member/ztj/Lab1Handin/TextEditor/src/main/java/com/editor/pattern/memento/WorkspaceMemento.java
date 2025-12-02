package com.editor.pattern.memento;

import com.google.gson.Gson;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;

/**
 * 工作区备忘录
 * 保存工作区的状态以便恢复
 */
public class WorkspaceMemento implements Memento {
    private List<String> openFiles;
    private String activeFile;
    private Deque<String> recentFiles;
    private static final Gson gson = new Gson();
    
    public WorkspaceMemento() {
        this.openFiles = new ArrayList<>();
        this.recentFiles = new ArrayDeque<>();
    }
    
    public WorkspaceMemento(List<String> openFiles, String activeFile, Deque<String> recentFiles) {
        this.openFiles = new ArrayList<>(openFiles);
        this.activeFile = activeFile;
        this.recentFiles = new ArrayDeque<>(recentFiles);
    }
    
    @Override
    public String getType() {
        return "WorkspaceMemento";
    }
    
    public List<String> getOpenFiles() {
        return openFiles;
    }
    
    public String getActiveFile() {
        return activeFile;
    }
    
    public Deque<String> getRecentFiles() {
        return recentFiles;
    }
    
    /**
     * 序列化到文件
     */
    public void saveToFile(String filepath) throws IOException {
        try (FileWriter writer = new FileWriter(filepath)) {
            gson.toJson(this, writer);
        }
    }
    
    /**
     * 从文件反序列化
     */
    public static WorkspaceMemento loadFromFile(String filepath) throws IOException {
        try (FileReader reader = new FileReader(filepath)) {
            return gson.fromJson(reader, WorkspaceMemento.class);
        }
    }
    
    /**
     * 转换为JSON字符串
     */
    public String toJson() {
        return gson.toJson(this);
    }
    
    /**
     * 从JSON字符串创建
     */
    public static WorkspaceMemento fromJson(String json) {
        return gson.fromJson(json, WorkspaceMemento.class);
    }
}

