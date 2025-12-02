"""
文本编辑器测试模块

测试TextEditor类的各种功能。
"""

import unittest
import os
import tempfile
from text_editor import TextEditor


class TestTextEditor(unittest.TestCase):
    """TextEditor类的测试用例"""
    
    def setUp(self):
        """每个测试方法执行前的准备工作"""
        self.editor = TextEditor("test.txt")
    
    def tearDown(self):
        """每个测试方法执行后的清理工作"""
        # 清理临时文件
        if os.path.exists("test.txt"):
            os.remove("test.txt")
    
    def test_append(self):
        """测试追加文本功能"""
        self.editor.append("Hello")
        self.assertEqual(self.editor.get_line(1), "Hello")
        self.assertEqual(self.editor.get_line_count(), 1)
        
        self.editor.append("World")
        self.assertEqual(self.editor.get_line(2), "World")
        self.assertEqual(self.editor.get_line_count(), 2)
    
    def test_append_multiline(self):
        """测试追加多行文本"""
        self.editor.append("Line1\nLine2\nLine3")
        self.assertEqual(self.editor.get_line_count(), 3)
        self.assertEqual(self.editor.get_line(1), "Line1")
        self.assertEqual(self.editor.get_line(2), "Line2")
        self.assertEqual(self.editor.get_line(3), "Line3")
    
    def test_insert(self):
        """测试插入文本功能"""
        self.editor.append("Hello World")
        # 列号6是空格，插入后应该是在空格之后
        # "Hello World" -> 在列号7（W的位置）插入 "Beautiful " 
        # 或者在列号6插入，但要理解列号6是空格位置，插入后空格保留
        # 实际上，根据需求，列号6插入应该是在空格位置插入，结果是 "Hello Beautiful  World"
        # 但测试期望是 "Hello Beautiful World"，说明测试可能期望在空格之后插入
        # 修正：在列号7（W的位置）插入
        self.editor.insert(1, 7, "Beautiful ")
        self.assertEqual(self.editor.get_line(1), "Hello Beautiful World")
    
    def test_insert_at_end(self):
        """测试在行尾插入文本"""
        self.editor.append("Hello")
        self.editor.insert(1, 6, " World")
        self.assertEqual(self.editor.get_line(1), "Hello World")
    
    def test_insert_empty_file(self):
        """测试在空文件中插入文本"""
        self.editor.insert(1, 1, "Hello")
        self.assertEqual(self.editor.get_line(1), "Hello")
    
    def test_insert_error_empty_file(self):
        """测试在空文件非1:1位置插入应该报错"""
        with self.assertRaises(ValueError):
            self.editor.insert(1, 2, "Hello")
    
    def test_delete(self):
        """测试删除文本功能"""
        self.editor.append("Hello World")
        self.editor.delete(1, 7, 5)
        self.assertEqual(self.editor.get_line(1), "Hello ")
    
    def test_replace(self):
        """测试替换文本功能"""
        self.editor.append("Hello World")
        self.editor.replace(1, 1, 5, "Hi")
        self.assertEqual(self.editor.get_line(1), "Hi World")
    
    def test_show(self):
        """测试显示文本功能"""
        self.editor.append("Line1")
        self.editor.append("Line2")
        self.editor.append("Line3")
        
        result = self.editor.show()
        self.assertIn("Line1", result)
        self.assertIn("Line2", result)
        self.assertIn("Line3", result)
    
    def test_show_range(self):
        """测试显示指定范围"""
        self.editor.append("Line1")
        self.editor.append("Line2")
        self.editor.append("Line3")
        
        result = self.editor.show(1, 2)
        self.assertIn("Line1", result)
        self.assertIn("Line2", result)
        self.assertNotIn("Line3", result)
    
    def test_save_and_load(self):
        """测试保存和加载文件"""
        self.editor.append("Line1")
        self.editor.append("Line2")
        
        # 保存到临时文件
        with tempfile.NamedTemporaryFile(mode='w', delete=False, suffix='.txt') as f:
            temp_path = f.name
        
        try:
            self.editor.save_to_file(temp_path)
            
            # 创建新编辑器加载文件
            new_editor = TextEditor(temp_path)
            new_editor.load_from_file(temp_path)
            
            self.assertEqual(new_editor.get_line_count(), 2)
            self.assertEqual(new_editor.get_line(1), "Line1")
            self.assertEqual(new_editor.get_line(2), "Line2")
        finally:
            if os.path.exists(temp_path):
                os.remove(temp_path)
    
    def test_modified_flag(self):
        """测试修改标记"""
        self.assertFalse(self.editor.is_modified())
        
        self.editor.append("Hello")
        self.assertTrue(self.editor.is_modified())
        
        self.editor.set_modified(False)
        self.assertFalse(self.editor.is_modified())


if __name__ == '__main__':
    unittest.main()

