// Main.java
package editor;

import editor.workspace.Workspace;
import editor.command.CommandParser;
import editor.command.Command;

import java.util.Scanner;

/**
 * 文本编辑器主程序入口
 */
public class Main {
    public static void main(String[] args) {
        Workspace workspace = new Workspace();
        workspace.loadWorkspaceState();

        CommandParser parser = new CommandParser(workspace);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Text Editor Started. Type 'exit' to quit.");

        while (true) {
            try {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                Command command = parser.parse(input);
                if (command != null) {
                    command.execute();

                    // 特殊处理exit命令
                    if (input.equals("exit")) {
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }
}