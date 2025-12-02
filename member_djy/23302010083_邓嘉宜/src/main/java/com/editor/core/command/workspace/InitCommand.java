package com.editor.core.command.workspace;

import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.editor.EditorException;
import com.editor.core.editor.TextEditor;
import com.editor.core.workspace.Workspace;

import java.io.File;

/**
 * init命令：创建新缓冲区
 * 功能：创建一个未保存的新缓冲文件，并初始化基础结构。
 * 参数：file（文件路径），可选 with-log（在第一行添加 # log）
 */
public class InitCommand implements Command {
    private Workspace workspace;
    private String filePath;
    private boolean withLog;
    
    public InitCommand(Workspace workspace, String filePath, boolean withLog) {
        this.workspace = workspace;
        this.filePath = filePath;
        this.withLog = withLog;
    }
    
    @Override
    public void execute() throws CommandException {
        // 检查文件是否在文件系统中已存在
        File file = new File(filePath);
        if (file.exists()) {
            throw new CommandException("文件已存在: " + filePath);
        }
        
        // 检查文件是否已经打开（在工作区）
        if (workspace.isFileOpen(filePath)) {
            throw new CommandException("文件已在工作区中打开: " + filePath);
        }
        
        // 创建新的编辑器
        TextEditor editor = new TextEditor(filePath);
        
        // 如果 with-log，添加第一行 "# log"
        if (withLog) {
            try {
                editor.insert(1, 1, "# log");
            } catch (EditorException e) {
                throw new CommandException("初始化文件内容失败: " + e.getMessage(), e);
            }
        }
        
        // 打开文件（添加到工作区）
        workspace.openFile(filePath, editor);
        
        // 如果 with-log，启用日志
        if (withLog) {
            try {
                workspace.enableLogging(filePath);
            } catch (IllegalStateException e) {
                throw new CommandException("启用日志失败: " + e.getMessage(), e);
            }
        }
        
        // 设置为活动文件
        workspace.setActiveFile(filePath);
        
        // 标记为已修改（因为是新缓冲区）
        workspace.updateModifiedStatus(filePath, true);
        
        // 通知命令执行
        workspace.notifyCommandExecuted("init", filePath, filePath);
    }
    
    @Override
    public String getCommandName() {
        return "init";
    }
    
    @Override
    public String getDescription() {
        return "创建新缓冲区（init <file> [with-log]）";
    }
}
