package com.editor.ui.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CommandParser 单元测试
 * 测试命令解析器的所有功能
 */
public class CommandParserTest {
    private CommandParser parser;
    
    @BeforeEach
    void setUp() {
        parser = new CommandParser();
    }
    
    // ========== 基本解析测试 ==========
    
    /**
     * 测试无参数简单命令的解析。
     * 测试数据：输入字符串 "load"。
     * 预期：命令名为 "load"，参数个数为 0。
     */
    @Test
    void testParseSimpleCommand() {
        CommandParser.ParsedCommand cmd = parser.parse("load");
        assertNotNull(cmd);
        assertEquals("load", cmd.getCommandName());
        assertEquals(0, cmd.getArgCount());
    }
    
    /**
     * 测试带一个参数的命令解析。
     * 测试数据：输入 "load test.txt"。
     * 预期：命令名为 "load"，只有一个参数 "test.txt"。
     */
    @Test
    void testParseCommandWithOneArg() {
        CommandParser.ParsedCommand cmd = parser.parse("load test.txt");
        assertNotNull(cmd);
        assertEquals("load", cmd.getCommandName());
        assertEquals(1, cmd.getArgCount());
        assertEquals("test.txt", cmd.getArg(0));
    }
    
    /**
     * 测试带多个参数（位置+文本）的命令解析。
     * 测试数据：输入 "insert 1:5 \"text\""。
     * 预期：命令名为 "insert"，参数为 "1:5" 和 "text"。
     */
    @Test
    void testParseCommandWithMultipleArgs() {
        CommandParser.ParsedCommand cmd = parser.parse("insert 1:5 \"text\"");
        assertNotNull(cmd);
        assertEquals("insert", cmd.getCommandName());
        assertEquals(2, cmd.getArgCount());
        assertEquals("1:5", cmd.getArg(0));
        assertEquals("text", cmd.getArg(1));
    }
    
    /**
     * 测试带引号参数的解析（去掉引号）。
     * 测试数据：输入 "append \"Hello World\""。
     * 预期：参数列表只有一个元素 "Hello World"，外层引号被去掉。
     */
    @Test
    void testParseCommandWithQuotedArg() {
        CommandParser.ParsedCommand cmd = parser.parse("append \"Hello World\"");
        assertNotNull(cmd);
        assertEquals("append", cmd.getCommandName());
        assertEquals(1, cmd.getArgCount());
        assertEquals("Hello World", cmd.getArg(0)); // 引号应该被移除
    }
    
    /**
     * 测试带空格的长引号参数解析。
     * 测试数据：输入 "append \"Hello World Test\""。
     * 预期：单个参数为 "Hello World Test"。
     */
    @Test
    void testParseCommandWithQuotedArgContainingSpaces() {
        CommandParser.ParsedCommand cmd = parser.parse("append \"Hello World Test\"");
        assertNotNull(cmd);
        assertEquals("append", cmd.getCommandName());
        assertEquals(1, cmd.getArgCount());
        assertEquals("Hello World Test", cmd.getArg(0));
    }
    
    /**
     * 测试 null 输入。
     * 测试数据：输入 null。
     * 预期：返回 null，表示不生成 ParsedCommand。
     */
    @Test
    void testParseNullInput() {
        CommandParser.ParsedCommand cmd = parser.parse(null);
        assertNull(cmd);
    }
    
    /**
     * 测试空字符串输入。
     * 测试数据：输入 ""。
     * 预期：返回 null。
     */
    @Test
    void testParseEmptyInput() {
        CommandParser.ParsedCommand cmd = parser.parse("");
        assertNull(cmd);
    }
    
    /**
     * 测试仅包含空白字符的输入。
     * 测试数据：输入 "   "。
     * 预期：返回 null。
     */
    @Test
    void testParseWhitespaceOnly() {
        CommandParser.ParsedCommand cmd = parser.parse("   ");
        assertNull(cmd);
    }
    
    // ========== 位置解析测试 ==========
    
    /**
     * 测试正常位置字符串的解析。
     * 测试数据："1:5"。
     * 预期：解析为 [1,5]。
     */
    @Test
    void testParsePosition() {
        int[] pos = parser.parsePosition("1:5");
        assertNotNull(pos);
        assertEquals(1, pos[0]);
        assertEquals(5, pos[1]);
    }
    
    /**
     * 测试大数字位置解析。
     * 测试数据："100:200"。
     * 预期：解析为 [100,200]。
     */
    @Test
    void testParsePositionLargeNumbers() {
        int[] pos = parser.parsePosition("100:200");
        assertNotNull(pos);
        assertEquals(100, pos[0]);
        assertEquals(200, pos[1]);
    }
    
    /**
     * 测试非法格式的位置字符串。
     * 测试数据："1-5"（使用横杠而不是冒号）。
     * 预期：返回 null，表示解析失败。
     */
    @Test
    void testParsePositionInvalidFormat() {
        int[] pos = parser.parsePosition("1-5");
        assertNull(pos);
    }
    
    /**
     * 测试无冒号的位置字符串。
     * 测试数据："15"。
     * 预期：返回 null。
     */
    @Test
    void testParsePositionNoColon() {
        int[] pos = parser.parsePosition("15");
        assertNull(pos);
    }
    
    /**
     * 测试包含非数字内容的位置字符串。
     * 测试数据："abc:def"。
     * 预期：返回 null。
     */
    @Test
    void testParsePositionInvalidNumber() {
        int[] pos = parser.parsePosition("abc:def");
        assertNull(pos);
    }
    
    /**
     * 测试 null 位置字符串。
     * 测试数据：null。
     * 预期：返回 null。
     */
    @Test
    void testParsePositionNull() {
        int[] pos = parser.parsePosition(null);
        assertNull(pos);
    }
    
    /**
     * 测试包含多个冒号的位置字符串。
     * 测试数据："1:2:3"。
     * 预期：返回 null，因为格式不符合要求。
     */
    @Test
    void testParsePositionMultipleColons() {
        int[] pos = parser.parsePosition("1:2:3");
        assertNull(pos); // 多个冒号应该返回null
    }
    
    // ========== 范围解析测试 ==========
    
    /**
     * 测试正常行范围解析。
     * 测试数据："1:10"。
     * 预期：解析为 [1,10]。
     */
    @Test
    void testParseRange() {
        int[] range = parser.parseRange("1:10");
        assertNotNull(range);
        assertEquals(1, range[0]);
        assertEquals(10, range[1]);
    }
    
    /**
     * 测试 parseRange 与 parsePosition 共享实现的情况。
     * 测试数据："5:20"。
     * 预期：解析为 [5,20]。
     */
    @Test
    void testParseRangeSameAsPosition() {
        // parseRange使用parsePosition的实现
        int[] range = parser.parseRange("5:20");
        assertNotNull(range);
        assertEquals(5, range[0]);
        assertEquals(20, range[1]);
    }
    
    // ========== 整数解析测试 ==========
    
    /**
     * 测试正常整数解析。
     * 测试数据："123"。
     * 预期：返回整型 123。
     */
    @Test
    void testParseInteger() {
        Integer value = parser.parseInteger("123");
        assertNotNull(value);
        assertEquals(123, value.intValue());
    }
    
    /**
     * 测试解析 0。
     * 测试数据："0"。
     * 预期：返回 0。
     */
    @Test
    void testParseIntegerZero() {
        Integer value = parser.parseInteger("0");
        assertNotNull(value);
        assertEquals(0, value.intValue());
    }
    
    /**
     * 测试解析负数。
     * 测试数据："-5"。
     * 预期：返回 -5。
     */
    @Test
    void testParseIntegerNegative() {
        Integer value = parser.parseInteger("-5");
        assertNotNull(value);
        assertEquals(-5, value.intValue());
    }
    
    /**
     * 测试非法整数字符串。
     * 测试数据："abc"。
     * 预期：返回 null。
     */
    @Test
    void testParseIntegerInvalid() {
        Integer value = parser.parseInteger("abc");
        assertNull(value);
    }
    
    /**
     * 测试 null 整数字符串。
     * 测试数据：null。
     * 预期：返回 null。
     */
    @Test
    void testParseIntegerNull() {
        Integer value = parser.parseInteger(null);
        assertNull(value);
    }
    
    /**
     * 测试空字符串整数字符串。
     * 测试数据：""。
     * 预期：返回 null。
     */
    @Test
    void testParseIntegerEmpty() {
        Integer value = parser.parseInteger("");
        assertNull(value);
    }
    
    /**
     * 测试转义字符串解析。
     * 测试数据："Line\\nTwo\\tTabbed\\\"Quote\\\\"。
     * 预期：转换为包含换行、制表符、双引号和反斜杠的字符串。
     */
    @Test
    void testUnescape() {
        String raw = "Line\\nTwo\\tTabbed\\\"Quote\\\\";
        String expected = "Line\nTwo\tTabbed\"Quote\\";
        assertEquals(expected, parser.unescape(raw));
    }
    
    // ========== 复杂命令测试 ==========
    
    /**
     * 测试复杂 replace 命令解析。
     * 测试数据："replace 1:5 3 \"new text\""。
     * 预期：命令名为 "replace"，参数依次为 "1:5"、"3"、"new text"。
     */
    @Test
    void testParseComplexCommand() {
        CommandParser.ParsedCommand cmd = parser.parse("replace 1:5 3 \"new text\"");
        assertNotNull(cmd);
        assertEquals("replace", cmd.getCommandName());
        assertEquals(3, cmd.getArgCount());
        assertEquals("1:5", cmd.getArg(0));
        assertEquals("3", cmd.getArg(1));
        assertEquals("new text", cmd.getArg(2));
    }
    
    /**
     * 测试带引号参数和普通参数混合的命令。
     * 测试数据："append \"text\" file.txt"。
     * 预期：参数 0 为 "text"，参数 1 为 "file.txt"。
     */
    @Test
    void testParseCommandWithMixedQuotes() {
        CommandParser.ParsedCommand cmd = parser.parse("append \"text\" file.txt");
        assertNotNull(cmd);
        assertEquals("append", cmd.getCommandName());
        assertEquals(2, cmd.getArgCount());
        assertEquals("text", cmd.getArg(0));
        assertEquals("file.txt", cmd.getArg(1));
    }
    
    /**
     * 测试 show 命令带行范围的解析。
     * 测试数据："show 1:10"。
     * 预期：命令名为 "show"，唯一参数为 "1:10"。
     */
    @Test
    void testParseShowCommandWithRange() {
        CommandParser.ParsedCommand cmd = parser.parse("show 1:10");
        assertNotNull(cmd);
        assertEquals("show", cmd.getCommandName());
        assertEquals(1, cmd.getArgCount());
        assertEquals("1:10", cmd.getArg(0));
    }
    
    /**
     * 测试 show 命令无参数解析。
     * 测试数据："show"。
     * 预期：命令名为 "show"，参数个数为 0。
     */
    @Test
    void testParseShowCommandNoArgs() {
        CommandParser.ParsedCommand cmd = parser.parse("show");
        assertNotNull(cmd);
        assertEquals("show", cmd.getCommandName());
        assertEquals(0, cmd.getArgCount());
    }
    
    // ========== ParsedCommand 类测试 ==========
    
    /**
     * 测试 ParsedCommand.getArg 的边界行为。
     * 测试数据：命令 "load test.txt"，访问索引 0、1、-1。
     * 预期：索引 0 返回 "test.txt"，越界和负数索引返回 null。
     */
    @Test
    void testParsedCommandGetArg() {
        CommandParser.ParsedCommand cmd = parser.parse("load test.txt");
        assertEquals("test.txt", cmd.getArg(0));
        assertNull(cmd.getArg(1)); // 越界返回null
        assertNull(cmd.getArg(-1)); // 负数返回null
    }
    
    /**
     * 测试 ParsedCommand.getArgCount 返回参数数量。
     * 测试数据："load"、"load test.txt"、"insert 1:5 \"text\"" 三种命令。
     * 预期：参数个数分别为 0、1、2。
     */
    @Test
    void testParsedCommandArgCount() {
        CommandParser.ParsedCommand cmd1 = parser.parse("load");
        assertEquals(0, cmd1.getArgCount());
        
        CommandParser.ParsedCommand cmd2 = parser.parse("load test.txt");
        assertEquals(1, cmd2.getArgCount());
        
        CommandParser.ParsedCommand cmd3 = parser.parse("insert 1:5 \"text\"");
        assertEquals(2, cmd3.getArgCount());
    }
    
    /**
     * 测试 ParsedCommand.getArgs 返回参数列表。
     * 测试数据："load test.txt"。
     * 预期：参数列表大小为 1，唯一元素为 "test.txt"。
     */
    @Test
    void testParsedCommandGetArgs() {
        CommandParser.ParsedCommand cmd = parser.parse("load test.txt");
        assertNotNull(cmd.getArgs());
        assertEquals(1, cmd.getArgs().size());
        assertEquals("test.txt", cmd.getArgs().get(0));
    }
}
