package lab1;

import lab1.presentation.CommandParser;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CommandParser parser = new CommandParser();

        System.out.println("欢迎使用文本编辑器 (Lab1)");
        System.out.println("输入 'exit' 退出程序。");

        while (true) {
            System.out.print("> "); // 打印命令提示符
            String commandLine = scanner.nextLine();

            if (commandLine.trim().isEmpty()) {
                continue; // 忽略空行
            }

            boolean shouldExit = parser.executeCommand(commandLine);
            if (shouldExit) {
                break; // 'exit' 命令被执行
            }
        }

        scanner.close();
        System.out.println("程序已退出。");
    }
}