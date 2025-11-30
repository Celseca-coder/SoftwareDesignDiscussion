package com.editor.core.command.workspace;

import com.editor.core.command.Command;
import com.editor.core.command.CommandException;
import com.editor.core.workspace.Workspace;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * dir-tree命令：显示目录树
 */
public class DirTreeCommand implements Command {
    private Workspace workspace;
    private String rootPath;
    private StringBuilder output;
    
    public DirTreeCommand(Workspace workspace, String rootPath) {
        this.workspace = workspace;
        this.rootPath = rootPath != null ? rootPath : ".";
        this.output = new StringBuilder();
    }
    
    @Override
    public void execute() throws CommandException {
        File root = new File(rootPath);
        if (!root.exists()) {
            throw new CommandException("目录不存在: " + rootPath);
        }
        
        if (!root.isDirectory()) {
            throw new CommandException("不是目录: " + rootPath);
        }
        
        output.append(rootPath).append("\n");
        printDirectoryTree(root, "", true);
        
        // 通知命令执行（显示类命令不改变状态）
        workspace.notifyCommandExecuted("dir-tree", rootPath, null);
    }
    
    private void printDirectoryTree(File dir, String prefix, boolean isLast) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        
        // 排序：目录在前，文件在后
        List<File> dirs = new ArrayList<>();
        List<File> fileList = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                dirs.add(file);
            } else {
                fileList.add(file);
            }
        }
        
        List<File> allFiles = new ArrayList<>();
        allFiles.addAll(dirs);
        allFiles.addAll(fileList);
        
        for (int i = 0; i < allFiles.size(); i++) {
            File file = allFiles.get(i);
            boolean isLastItem = (i == allFiles.size() - 1);
            
            String connector = isLastItem ? "└── " : "├── ";
            output.append(prefix).append(connector).append(file.getName());
            
            // 标记是否已打开
            if (workspace.isFileOpen(file.getPath())) {
                output.append(" [已打开]");
            }
            
            output.append("\n");
            
            // 如果是目录，递归打印
            if (file.isDirectory()) {
                String newPrefix = prefix + (isLastItem ? "    " : "│   ");
                printDirectoryTree(file, newPrefix, isLastItem);
            }
        }
    }
    
    public String getOutput() {
        return output.toString();
    }
    
    @Override
    public String getCommandName() {
        return "dir-tree";
    }
    
    @Override
    public String getDescription() {
        return "显示目录树";
    }
}
