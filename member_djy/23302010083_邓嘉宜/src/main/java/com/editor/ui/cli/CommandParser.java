package com.editor.ui.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 命令解析器
 * 解析用户输入的命令和参数
 */
public class CommandParser {
    
    /**
     * 解析命令
     * @param input 用户输入
     * @return 解析结果
     */
    public ParsedCommand parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        
        input = input.trim();
        String[] parts = splitCommand(input);
        
        if (parts.length == 0) {
            return null;
        }
        
        String commandName = parts[0];
        List<String> args = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            args.add(parts[i]);
        }
        
        return new ParsedCommand(commandName, args);
    }
    
    /**
     * 分割命令，支持引号内的字符串
     */
    private String[] splitCommand(String input) {
        List<String> parts = new ArrayList<>();
        Pattern pattern = Pattern.compile("([^\"\\s]+|\".+?\")\\s*");
        Matcher matcher = pattern.matcher(input);
        
        while (matcher.find()) {
            String part = matcher.group(1);
            // 移除引号
            if (part.startsWith("\"") && part.endsWith("\"")) {
                part = part.substring(1, part.length() - 1);
            }
            parts.add(part);
        }
        
        return parts.toArray(new String[0]);
    }
    
    /**
     * 解析位置参数 (line:col)
     * @param arg 参数字符串，格式如 "1:5"
     * @return [line, col]，如果解析失败返回null
     */
    public int[] parsePosition(String arg) {
        if (arg == null || !arg.contains(":")) {
            return null;
        }
        
        try {
            String[] parts = arg.split(":");
            if (parts.length != 2) {
                return null;
            }
            
            int line = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            return new int[]{line, col};
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 解析行范围 (startLine:endLine)
     * @param arg 参数字符串，格式如 "1:10"
     * @return [startLine, endLine]，如果解析失败返回null
     */
    public int[] parseRange(String arg) {
        return parsePosition(arg); // 格式相同
    }
    
    /**
     * 解析整数参数
     */
    public Integer parseInteger(String arg) {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 解析字符串中的转义字符（如 \n、\t、\\、\" 等）
     */
    public String unescape(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder sb = new StringBuilder();
        boolean escaping = false;
        for (char c : input.toCharArray()) {
            if (!escaping) {
                if (c == '\\') {
                    escaping = true;
                } else {
                    sb.append(c);
                }
            } else {
                switch (c) {
                    case 'n':
                        sb.append('\n');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    default:
                        sb.append(c);
                        break;
                }
                escaping = false;
            }
        }
        if (escaping) {
            sb.append('\\');
        }
        return sb.toString();
    }
    
    /**
     * 解析的命令结果
     */
    public static class ParsedCommand {
        private String commandName;
        private List<String> args;
        
        public ParsedCommand(String commandName, List<String> args) {
            this.commandName = commandName;
            this.args = args;
        }
        
        public String getCommandName() {
            return commandName;
        }
        
        public List<String> getArgs() {
            return args;
        }
        
        public String getArg(int index) {
            if (index >= 0 && index < args.size()) {
                return args.get(index);
            }
            return null;
        }
        
        public int getArgCount() {
            return args.size();
        }
    }
}
