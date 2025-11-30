package com.editor.core.command.workspace;

import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.editor.Editor;
import com.editor.core.editor.TextEditor;
import com.editor.core.persistence.FilePersistence;
import com.editor.core.workspace.Workspace;

import java.io.IOException;
import java.util.Scanner;

/**
 * close命令：关闭文件
 * 如果文件已修改，会提示用户是否保存
 */
public class CloseCommand implements Command {
    private Workspace workspace;
    private FilePersistence filePersistence;
    private String filePath;
    private Scanner scanner;
    
    public CloseCommand(Workspace workspace, FilePersistence filePersistence, String filePath, Scanner scanner) {
        this.workspace = workspace;
        this.filePersistence = filePersistence;
        this.filePath = filePath;
        this.scanner = scanner;
    }
    
    @Override
    public void execute() throws CommandException {
        if (filePath == null) {
            filePath = workspace.getActiveFile();
        }
        
        if (filePath == null) {
            throw new CommandException("没有指定要关闭的文件");
        }
        
        if (!workspace.isFileOpen(filePath)) {
            throw new CommandException("文件未打开: " + filePath);
        }
        
        // 检查文件是否已修改
        if (workspace.isFileModified(filePath)) {
            // 提示用户是否保存
            System.out.print("文件已修改，是否保存? (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase();
            
            if (response.equals("y") || response.equals("yes")) {
                // 保存文件
                try {
                    Editor editor = workspace.getEditor(filePath);
                    if (editor instanceof TextEditor) {
                        TextEditor textEditor = (TextEditor) editor;
                        filePersistence.saveFile(filePath, textEditor.getLines());
                        textEditor.setModified(false);
                        workspace.updateModifiedStatus(filePath, false);
                        workspace.notifyFileSaved(filePath);
                    }
                } catch (IOException e) {
                    throw new CommandException("保存文件失败: " + e.getMessage(), e);
                }
            } else if (!response.equals("n") && !response.equals("no")) {
                // 无效输入，取消关闭操作
                System.out.println("操作已取消");
                return;
            }
            // 如果用户选择 'n'，直接关闭不保存
        }
        
        // 关闭文件
        workspace.closeFile(filePath);
        
        // 通知命令执行
        workspace.notifyCommandExecuted("close", filePath, filePath);
    }
    
    @Override
    public String getCommandName() {
        return "close";
    }
    
    @Override
    public String getDescription() {
        return "关闭文件";
    }
}
