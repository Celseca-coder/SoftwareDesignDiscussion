package com.editor.core.persistence;

import com.editor.core.workspace.WorkspaceMemento;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作区持久化服务
 * 负责保存和加载工作区状态到 .editor_workspace 文件
 * 使用简单的文本格式保存
 */
public class WorkspacePersistence {
    private static final String WORKSPACE_FILE = ".editor_workspace";
    private final Path workspaceFile;

    public WorkspacePersistence() {
        this(Paths.get(WORKSPACE_FILE));
    }

    public WorkspacePersistence(Path workspaceFile) {
        this.workspaceFile = workspaceFile;
    }
    
    /**
     * 保存工作区状态
     */
    public void save(WorkspaceMemento memento) throws IOException {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(
                    new FileOutputStream(workspaceFile.toFile()), 
                    StandardCharsets.UTF_8))) {
            
            // 保存打开的文件列表
            List<String> openFiles = memento.getOpenFiles();
            writer.println("# openFiles");
            for (String file : openFiles) {
                writer.println(file);
            }
            writer.println("# endOpenFiles");
            
            // 保存活动文件
            writer.println("# activeFile");
            String activeFile = memento.getActiveFile();
            writer.println(activeFile != null ? activeFile : "");
            writer.println("# endActiveFile");
            
            // 保存修改状态
            writer.println("# modifiedStatus");
            Map<String, Boolean> modifiedStatus = memento.getModifiedStatus();
            for (Map.Entry<String, Boolean> entry : modifiedStatus.entrySet()) {
                writer.println(entry.getKey() + "=" + entry.getValue());
            }
            writer.println("# endModifiedStatus");
            
            // 保存日志开关状态
            writer.println("# loggingEnabled");
            Map<String, Boolean> loggingEnabled = memento.getLoggingEnabled();
            for (Map.Entry<String, Boolean> entry : loggingEnabled.entrySet()) {
                writer.println(entry.getKey() + "=" + entry.getValue());
            }
            writer.println("# endLoggingEnabled");
        }
    }
    
    /**
     * 加载工作区状态
     */
    public WorkspaceMemento load() throws IOException {
        Path path = this.workspaceFile;
        if (!Files.exists(path)) {
            return new WorkspaceMemento(
                new ArrayList<>(),
                null,
                new HashMap<>(),
                new HashMap<>()
            );
        }
        
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        
        List<String> openFiles = new ArrayList<>();
        String activeFile = null;
        Map<String, Boolean> modifiedStatus = new HashMap<>();
        Map<String, Boolean> loggingEnabled = new HashMap<>();
        
        String currentSection = null;
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.equals("# openFiles")) {
                currentSection = "openFiles";
            } else if (line.equals("# endOpenFiles")) {
                currentSection = null;
            } else if (line.equals("# activeFile")) {
                currentSection = "activeFile";
            } else if (line.equals("# endActiveFile")) {
                currentSection = null;
            } else if (line.equals("# modifiedStatus")) {
                currentSection = "modifiedStatus";
            } else if (line.equals("# endModifiedStatus")) {
                currentSection = null;
            } else if (line.equals("# loggingEnabled")) {
                currentSection = "loggingEnabled";
            } else if (line.equals("# endLoggingEnabled")) {
                currentSection = null;
            } else if (currentSection != null && !line.isEmpty()) {
                switch (currentSection) {
                    case "openFiles":
                        openFiles.add(line);
                        break;
                    case "activeFile":
                        activeFile = line.isEmpty() ? null : line;
                        break;
                    case "modifiedStatus":
                        parseKeyValue(line, modifiedStatus);
                        break;
                    case "loggingEnabled":
                        parseKeyValue(line, loggingEnabled);
                        break;
                }
            }
        }
        
        return new WorkspaceMemento(openFiles, activeFile, modifiedStatus, loggingEnabled);
    }
    
    /**
     * 解析键值对
     */
    private void parseKeyValue(String line, Map<String, Boolean> map) {
        int eqIndex = line.indexOf('=');
        if (eqIndex > 0) {
            String key = line.substring(0, eqIndex);
            String value = line.substring(eqIndex + 1);
            map.put(key, Boolean.parseBoolean(value));
        }
    }
}
