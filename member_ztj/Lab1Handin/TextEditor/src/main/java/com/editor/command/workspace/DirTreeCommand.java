package com.editor.command.workspace;

import com.editor.command.Command;
import com.editor.util.TreeBuilder;

/**
 * 显示目录树命令
 */
public class DirTreeCommand implements Command {
    private String dirpath;
    private int maxDepth;
    
    public DirTreeCommand(String dirpath) {
        this(dirpath, 3);
    }
    
    public DirTreeCommand(String dirpath, int maxDepth) {
        this.dirpath = dirpath;
        this.maxDepth = maxDepth;
    }
    
    @Override
    public void execute() throws Exception {
        String tree = TreeBuilder.buildSimpleTree(dirpath, maxDepth);
        System.out.println(tree);
    }
    
    @Override
    public void undo() throws Exception {
        // DirTree命令不需要撤销
    }
    
    @Override
    public boolean isUndoable() {
        return false;
    }
    
    @Override
    public String getDescription() {
        return "显示目录树: " + dirpath;
    }
}

