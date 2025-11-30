package com.editor;

import com.editor.core.Application;
import com.editor.core.command.CommandManager;
import com.editor.core.editor.Editor;
import com.editor.core.editor.TextEditor;
import com.editor.core.logging.LoggingService;
import com.editor.core.persistence.FilePersistence;
import com.editor.core.persistence.WorkspacePersistence;
import com.editor.core.workspace.Workspace;
import com.editor.core.workspace.WorkspaceMemento;
import com.editor.ui.cli.CommandLineInterface;

import java.io.IOException;
import java.util.List;

/**
 * 程序入口类
 */
public class Main {
    public static void main(String[] args) {
        try {
            // 初始化各个模块
            Application application = new Application();
            FilePersistence filePersistence = new FilePersistence();
            WorkspacePersistence workspacePersistence = new WorkspacePersistence();
            LoggingService loggingService = new LoggingService(filePersistence);
            Workspace workspace = new Workspace(loggingService);
            CommandManager commandManager = new CommandManager();
            
            // 尝试加载工作区状态
            try {
                WorkspaceMemento memento = workspacePersistence.load();
                workspace.restoreState(memento);
                
                // 恢复打开的文件：为每个已打开的文件重新加载内容并创建编辑器
                List<String> openFiles = memento.getOpenFiles();
                String activeFile = memento.getActiveFile();
                
                for (String filePath : openFiles) {
                    try {
                        // 检查文件是否存在（如果文件已被删除，跳过恢复）
                        if (!filePersistence.fileExists(filePath)) {
                            System.err.println("警告: 文件不存在，跳过恢复: " + filePath);
                            continue;
                        }
                        
                        // 加载文件内容
                        List<String> lines = filePersistence.loadFile(filePath);
                        
                        // 创建编辑器实例
                        Editor editor = new TextEditor(filePath, lines);
                        
                        // 恢复修改状态（在打开文件之前设置，确保状态正确同步）
                        boolean wasModified = memento.getModifiedStatus().getOrDefault(filePath, false);
                        if (wasModified) {
                            editor.setModified(true);
                        }
                        
                        // 打开文件（这会添加到editorManager中，并同步修改状态）
                        workspace.openFile(filePath, editor);
                        
                        // 恢复日志状态
                        if (memento.getLoggingEnabled().getOrDefault(filePath, false)) {
                            workspace.enableLogging(filePath);
                        }
                    } catch (IOException e) {
                        // 文件可能已被删除或无法读取，跳过该文件
                        System.err.println("警告: 无法恢复文件 " + filePath + ": " + e.getMessage());
                    }
                }
                
                // 恢复活动文件（如果存在且已成功打开）
                if (activeFile != null && workspace.isFileOpen(activeFile)) {
                    workspace.setActiveFile(activeFile);
                } else if (!openFiles.isEmpty() && workspace.isFileOpen(openFiles.get(0))) {
                    // 如果活动文件无法恢复，使用第一个打开的文件
                    workspace.setActiveFile(openFiles.get(0));
                }
                
            } catch (IOException e) {
                // 首次运行，忽略错误
                System.out.println("提示: 未找到工作区状态文件，将创建新的工作区");
            }
            
            // 创建命令行界面
            CommandLineInterface cli = new CommandLineInterface(
                workspace, commandManager, filePersistence, 
                workspacePersistence, loggingService, application);
            
            // 启动命令行界面
            cli.start();
            
            // 程序退出前保存工作区状态
            try {
                workspacePersistence.save(workspace.saveState());
            } catch (IOException e) {
                System.err.println("警告: 保存工作区状态失败: " + e.getMessage());
            }
            
            cli.close();
            
        } catch (Exception e) {
            System.err.println("程序启动失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
