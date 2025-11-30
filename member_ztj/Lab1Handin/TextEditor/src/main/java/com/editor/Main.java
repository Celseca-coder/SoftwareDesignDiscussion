package com.editor;

import com.editor.core.Workspace;
import com.editor.cli.REPL;

/**
 * 程序入口
 */
public class Main {
    public static void main(String[] args) {
        try {
            // 创建工作区
            Workspace workspace = new Workspace();
            
            // 启动REPL
            REPL repl = new REPL(workspace);
            repl.start();
            repl.close();
            
        } catch (Exception e) {
            System.err.println("程序启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

