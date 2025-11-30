package com.editor.command.workspace;

import com.editor.command.Command;
import com.editor.core.Workspace;
import com.editor.util.FileUtil;
import java.io.IOException;

/**
 * 创建文件命令
 * 创建新文件并打开
 */
public class CreateCommand implements Command {
    private Workspace workspace;
    private String filepath;
    private boolean fileExisted;
    
    public CreateCommand(Workspace workspace, String filepath) {
        this.workspace = workspace;
        this.filepath = filepath;
        this.fileExisted = FileUtil.exists(filepath);
    }
    
    @Override
    public void execute() throws Exception {
        // 检查文件是否已存在
        if (fileExisted) {
            System.out.println("警告: 文件已存在，将覆盖: " + filepath);
        }
        
        // 创建父目录（如果不存在）
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(filepath);
            java.nio.file.Path parent = path.getParent();
            if (parent != null && !java.nio.file.Files.exists(parent)) {
                FileUtil.createDirectories(parent.toString());
            }
        } catch (Exception e) {
            // 忽略创建目录失败的情况
        }
        
        // 创建空文件
        try {
            FileUtil.writeFile(filepath, "");
        } catch (IOException e) {
            throw new Exception("创建文件失败: " + e.getMessage(), e);
        }
        
        // 加载并打开文件
        workspace.loadFile(filepath);
        System.out.println("已创建并打开文件: " + filepath);
    }
    
    @Override
    public void undo() throws Exception {
        // 关闭文件
        workspace.closeFile(filepath);
        
        // 如果文件原本不存在，则删除
        if (!fileExisted) {
            try {
                java.nio.file.Files.delete(java.nio.file.Paths.get(filepath));
            } catch (IOException e) {
                // 忽略删除失败
            }
        }
        System.out.println("已撤销创建文件: " + filepath);
    }
    
    @Override
    public boolean isUndoable() {
        return true;
    }
    
    @Override
    public String getDescription() {
        return "创建文件: " + filepath;
    }
}

