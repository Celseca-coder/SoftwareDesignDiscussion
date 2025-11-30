"""
命令模式测试模块

测试Command类的undo/redo功能。
"""

import unittest
from text_editor import TextEditor
from command import AppendCommand, InsertCommand, DeleteCommand, ReplaceCommand


class TestCommand(unittest.TestCase):
    """Command类的测试用例"""
    
    def setUp(self):
        """每个测试方法执行前的准备工作"""
        self.editor = TextEditor("test.txt")
    
    def test_append_command_undo_redo(self):
        """测试追加命令的撤销和重做"""
        command = AppendCommand(self.editor, "Hello")
        
        # 执行命令
        command.execute()
        self.assertEqual(self.editor.get_line(1), "Hello")
        
        # 撤销
        command.undo()
        self.assertEqual(self.editor.get_line_count(), 0)
        
        # 重做
        command.redo()
        self.assertEqual(self.editor.get_line(1), "Hello")
    
    def test_insert_command_undo_redo(self):
        """测试插入命令的撤销和重做"""
        self.editor.append("Hello World")
        original_content = self.editor.get_content()
        
        # 在列号7（W的位置）插入，而不是列号6（空格位置）
        command = InsertCommand(self.editor, 1, 7, "Beautiful ")
        
        # 执行命令
        command.execute()
        self.assertEqual(self.editor.get_line(1), "Hello Beautiful World")
        
        # 撤销
        command.undo()
        self.assertEqual(self.editor.get_content(), original_content)
        
        # 重做
        command.redo()
        self.assertEqual(self.editor.get_line(1), "Hello Beautiful World")
    
    def test_delete_command_undo_redo(self):
        """测试删除命令的撤销和重做"""
        self.editor.append("Hello World")
        original_content = self.editor.get_content()
        
        command = DeleteCommand(self.editor, 1, 7, 5)
        
        # 执行命令
        command.execute()
        self.assertEqual(self.editor.get_line(1), "Hello ")
        
        # 撤销
        command.undo()
        self.assertEqual(self.editor.get_content(), original_content)
        
        # 重做
        command.redo()
        self.assertEqual(self.editor.get_line(1), "Hello ")
    
    def test_replace_command_undo_redo(self):
        """测试替换命令的撤销和重做"""
        self.editor.append("Hello World")
        original_content = self.editor.get_content()
        
        command = ReplaceCommand(self.editor, 1, 1, 5, "Hi")
        
        # 执行命令
        command.execute()
        self.assertEqual(self.editor.get_line(1), "Hi World")
        
        # 撤销
        command.undo()
        self.assertEqual(self.editor.get_content(), original_content)
        
        # 重做
        command.redo()
        self.assertEqual(self.editor.get_line(1), "Hi World")


if __name__ == '__main__':
    unittest.main()

