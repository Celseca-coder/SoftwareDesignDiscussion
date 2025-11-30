package com.editor.core.command.workspace;

import com.editor.core.Application;
import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.persistence.FilePersistence;
import com.editor.core.persistence.WorkspacePersistence;
import com.editor.core.workspace.Workspace;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * exit命令：退出程序
 */
public class ExitCommand implements Command {
    private Workspace workspace;
    private WorkspacePersistence workspacePersistence;
    private FilePersistence filePersistence;
    private Application application;
    
    public ExitCommand(Workspace workspace, WorkspacePersistence workspacePersistence, 
                      FilePersistence filePersistence, Application application) {
        this.workspace = workspace;
        this.workspacePersistence = workspacePersistence;
        this.filePersistence = filePersistence;
        this.application = application;
    }
    
    @Override
    public void execute() throws CommandException {
        // 检查是否有未保存的文件
        if (workspace.hasUnsavedFiles()) {
            List<String> unsavedFiles = workspace.getUnsavedFiles();
            
            Scanner scanner = new Scanner(System.in);
            for (String filePath : unsavedFiles) {
                System.out.print("文件 " + filePath + " 已修改，是否保存？(y/n): ");
                String answer = scanner.nextLine().trim().toLowerCase();
                
                if (answer.equals("y") || answer.equals("yes")) {
                    // 保存文件
                    try {
                        SaveCommand saveCommand = new SaveCommand(workspace, filePersistence, filePath);
                        saveCommand.execute();
                    } catch (Exception e) {
                        System.err.println("保存文件失败: " + e.getMessage());
                    }
                }
            }
        }
        
        // 保存工作区状态
        try {
            workspacePersistence.save(workspace.saveState());
        } catch (IOException e) {
            System.err.println("保存工作区状态失败: " + e.getMessage());
        }
        
        // 退出程序
        if (application != null) {
            application.exit();
        }
        
        workspace.notifyCommandExecuted("exit", "", null);
    }
    
    @Override
    public String getCommandName() {
        return "exit";
    }
    
    @Override
    public String getDescription() {
        return "退出程序";
    }
}
