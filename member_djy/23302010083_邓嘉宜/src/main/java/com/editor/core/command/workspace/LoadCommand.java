package com.editor.core.command.workspace;

import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.editor.Editor;
import com.editor.core.editor.EditorException;
import com.editor.core.editor.TextEditor;
import com.editor.core.persistence.FilePersistence;
import com.editor.core.workspace.Workspace;

import java.io.IOException;
import java.util.List;

/**
 * load命令：加载文件到编辑器
 */
public class LoadCommand implements Command {
    private Workspace workspace;
    private FilePersistence filePersistence;
    private String filePath;
    
    public LoadCommand(Workspace workspace, FilePersistence filePersistence, String filePath) {
        this.workspace = workspace;
        this.filePersistence = filePersistence;
        this.filePath = filePath;
    }
    
    @Override
    public void execute() throws CommandException {
        try {
            // 检查文件是否已经打开
            if (workspace.isFileOpen(filePath)) {
                workspace.setActiveFile(filePath);
                return;
            }
            
            // 加载文件内容
            List<String> lines = filePersistence.loadFile(filePath);
            
            // 创建编辑器
            Editor editor = new TextEditor(filePath, lines);
            
            // 打开文件
            workspace.openFile(filePath, editor);
            
            // 设置为活动文件
            workspace.setActiveFile(filePath);
            
            // 通知命令执行
            workspace.notifyCommandExecuted("load", filePath, filePath);
            
        } catch (IOException e) {
            throw new CommandException("加载文件失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getCommandName() {
        return "load";
    }
    
    @Override
    public String getDescription() {
        return "加载文件到编辑器";
    }
}
