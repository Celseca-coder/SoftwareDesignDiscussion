"""
命令解析器测试模块

测试CommandParser类的解析功能。
"""

import unittest
from command_parser import CommandParser


class TestCommandParser(unittest.TestCase):
    """CommandParser类的测试用例"""
    
    def setUp(self):
        """每个测试方法执行前的准备工作"""
        self.parser = CommandParser()
    
    def test_parse_simple_command(self):
        """测试解析简单命令"""
        command, args = self.parser.parse("load test.txt")
        self.assertEqual(command, "load")
        self.assertEqual(args, ["test.txt"])
    
    def test_parse_command_with_quoted_text(self):
        """测试解析带引号的命令"""
        command, args = self.parser.parse('append "Hello World"')
        self.assertEqual(command, "append")
        self.assertEqual(args, ["Hello World"])
    
    def test_parse_command_with_multiple_args(self):
        """测试解析多个参数的命令"""
        command, args = self.parser.parse('insert 1:5 "text"')
        self.assertEqual(command, "insert")
        self.assertEqual(len(args), 2)
        self.assertEqual(args[0], "1:5")
        self.assertEqual(args[1], "text")
    
    def test_parse_line_col(self):
        """测试解析行号:列号"""
        line, col = self.parser.parse_line_col("10:20")
        self.assertEqual(line, 10)
        self.assertEqual(col, 20)
    
    def test_parse_line_col_error(self):
        """测试解析行号:列号错误格式"""
        with self.assertRaises(ValueError):
            self.parser.parse_line_col("10")
        
        with self.assertRaises(ValueError):
            self.parser.parse_line_col("10:20:30")
    
    def test_parse_range(self):
        """测试解析范围"""
        start, end = self.parser.parse_range("1:10")
        self.assertEqual(start, 1)
        self.assertEqual(end, 10)
    
    def test_parse_range_single_line(self):
        """测试解析单行范围"""
        start, end = self.parser.parse_range("5")
        self.assertEqual(start, 5)
        self.assertEqual(end, 5)


if __name__ == '__main__':
    unittest.main()

