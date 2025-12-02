package com.editor.cli;

import com.editor.core.Workspace;
import com.editor.logging.LogManager;
import com.editor.exception.InvalidCommandException;
import java.util.Scanner;

/**
 * REPL - 交互式循环
 * Read-Eval-Print-Loop
 */
public class REPL {
    private CommandParser parser;
    private CommandExecutor executor;
    private Scanner scanner;
    private boolean running;
    
    public REPL(Workspace workspace) {
        this.parser = new CommandParser(workspace);
        this.executor = new CommandExecutor();
        this.scanner = new Scanner(System.in);
        this.running = true;
    }
    
    /**
     * 启动交互式循环
     */
    public void start() {
        System.out.println("文本编辑器 v1.0");
        System.out.println("输入 'help' 或 'h' 查看帮助");
        System.out.println("输入 'quit' 或 'exit' 退出");
        System.out.println();
        
        while (running) {
            try {
                // 读取输入
                System.out.print("> ");
                String input = scanner.nextLine();
                
                // 解析命令
                var command = parser.parse(input);
                
                // 检查退出命令
                if (command == null) {
                    shutdown();
                    break;
                }
                
                // 执行命令
                executor.execute(command);
                
            } catch (InvalidCommandException e) {
                System.err.println("错误: " + e.getMessage());
                LogManager.error("命令错误: " + e.getMessage(), "REPL");
            } catch (Exception e) {
                System.err.println("执行失败: " + e.getMessage());
                LogManager.error("执行错误: " + e.getMessage(), "REPL");
                if (e.getCause() != null) {
                    System.err.println("原因: " + e.getCause().getMessage());
                }
            }
            System.out.println();
        }
    }
    
    /**
     * 关闭REPL
     */
    public void shutdown() {
        running = false;
        System.out.println("再见！");
        LogManager.info("程序退出", "REPL");
    }
    
    /**
     * 关闭资源
     */
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}

