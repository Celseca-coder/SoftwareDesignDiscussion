package com.editor.core.command.workspace;

import com.editor.core.command.CommandException;
import com.editor.core.command.UndoableCommand;
import com.editor.core.editor.Editor;
import com.editor.core.editor.TextEditor;
import com.editor.core.persistence.FilePersistence;
import com.editor.core.workspace.Workspace;

import java.io.IOException;
import java.util.List;
import java.util.List;

/**
 * save命令：保存文件
 * 支持语法：save（活动文件）、save <file>（指定文件）、save all（所有文件）
 */
public class SaveCommand implements UndoableCommand {
    private Workspace workspace;
    private FilePersistence filePersistence;
    private String arg; // 参数：文件路径或"all"
    private boolean wasModified; // 用于undo
    private List<String> savedFiles; // 用于"all"时的undo
    
    public SaveCommand(Workspace workspace, FilePersistence filePersistence, String arg) {
        this.workspace = workspace;
        this.filePersistence = filePersistence;
        this.arg = arg;
    }
    
    @Override
    public void execute() throws CommandException {
        if ("all".equalsIgnoreCase(arg)) {
            // 保存所有打开文件
            List<String> openFiles = workspace.getOpenFiles();
            if (openFiles.isEmpty()) {
                throw new CommandException("没有打开的文件");
            }
            savedFiles = openFiles;
            for (String filePath : openFiles) {
                if (!workspace.isFileModified(filePath)) {
                    continue; // 跳过未修改文件
                }
                saveSingleFile(filePath);
            }
            workspace.notifyCommandExecuted("save", "all", openFiles.get(0)); // 通知第一个文件
        } else {
            // 保存单个文件（指定或活动）
            String filePath = arg != null ? arg : workspace.getActiveFile();
            if (filePath == null) {
                throw new CommandException("没有指定要保存的文件");
            }
            saveSingleFile(filePath);
            workspace.notifyCommandExecuted("save", filePath, filePath);
        }
    }
    
    private void saveSingleFile(String filePath) throws CommandException {
        if (!workspace.isFileOpen(filePath)) {
            throw new CommandException("文件未打开: " + filePath);
        }
        
        Editor editor = workspace.getEditor(filePath);
        if (!(editor instanceof TextEditor)) {
            throw new CommandException("不支持的文件类型");
        }
        
        try {
            TextEditor textEditor = (TextEditor) editor;
            wasModified = textEditor.isModified();
            
            // 保存文件
            filePersistence.saveFile(filePath, textEditor.getLines());
            
            // 标记为未修改
            textEditor.setModified(false);
            workspace.updateModifiedStatus(filePath, false);
            
            // 通知文件保存事件
            workspace.notifyFileSaved(filePath);
            
        } catch (IOException e) {
            throw new CommandException("保存文件失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void undo() throws CommandException {
        if ("all".equalsIgnoreCase(arg) && savedFiles != null) {
            // 恢复所有保存文件的修改状态
            for (String filePath : savedFiles) {
                Editor editor = workspace.getEditor(filePath);
                if (editor instanceof TextEditor) {
                    ((TextEditor) editor).setModified(true);
                    workspace.updateModifiedStatus(filePath, true);
                }
            }
        } else {
            // 恢复单个文件的修改状态
            String filePath = arg != null ? arg : workspace.getActiveFile();
            if (filePath != null) {
                Editor editor = workspace.getEditor(filePath);
                if (editor instanceof TextEditor) {
                    ((TextEditor) editor).setModified(wasModified);
                    workspace.updateModifiedStatus(filePath, wasModified);
                }
            }
        }
    }
    
    @Override
    public boolean isUndoable() {
        return false; // save命令通常不进入撤销栈
    }
    
    @Override
    public String getCommandName() {
        return "save";
    }
    
    @Override
    public String getDescription() {
        return "保存文件（save [file|all]）";
    }
}
