"""
工作区测试模块

测试Workspace类的各种功能。
"""

import unittest
import os
import tempfile
import json
from workspace import Workspace
from text_editor import TextEditor


class TestWorkspace(unittest.TestCase):
    """Workspace类的测试用例"""
    
    def setUp(self):
        """每个测试方法执行前的准备工作"""
        self.workspace = Workspace()
        # 清理可能存在的workspace文件
        if os.path.exists(Workspace.WORKSPACE_FILE):
            os.remove(Workspace.WORKSPACE_FILE)
    
    def tearDown(self):
        """每个测试方法执行后的清理工作"""
        # 清理workspace文件
        if os.path.exists(Workspace.WORKSPACE_FILE):
            os.remove(Workspace.WORKSPACE_FILE)
    
    def test_load_file(self):
        """测试加载文件功能"""
        # 创建临时文件
        with tempfile.NamedTemporaryFile(mode='w', delete=False, suffix='.txt') as f:
            f.write("Line1\nLine2")
            temp_path = f.name
        
        try:
            self.assertTrue(self.workspace.load_file(temp_path))
            editor = self.workspace.get_active_editor()
            self.assertIsNotNone(editor)
            self.assertEqual(editor.editor.get_line_count(), 2)
        finally:
            if os.path.exists(temp_path):
                os.remove(temp_path)
    
    def test_init_file(self):
        """测试创建新文件功能"""
        with tempfile.NamedTemporaryFile(delete=False, suffix='.txt') as f:
            temp_path = f.name
        
        # 删除文件，使其不存在
        if os.path.exists(temp_path):
            os.remove(temp_path)
        
        try:
            self.assertTrue(self.workspace.init_file(temp_path))
            editor = self.workspace.get_active_editor()
            self.assertIsNotNone(editor)
            self.assertEqual(editor.editor.get_file_path(), temp_path)
        finally:
            if os.path.exists(temp_path):
                os.remove(temp_path)
    
    def test_save_file(self):
        """测试保存文件功能"""
        # 使用临时文件名，确保文件不存在
        import uuid
        temp_path = f"test_save_{uuid.uuid4().hex}.txt"
        
        try:
            # 确保文件不存在
            if os.path.exists(temp_path):
                os.remove(temp_path)
            
            self.workspace.init_file(temp_path)
            editor_wrapper = self.workspace.get_active_editor()
            self.assertIsNotNone(editor_wrapper, "编辑器应该已创建")
            editor_wrapper.editor.append("Test line")
            
            self.assertTrue(self.workspace.save_file())
            
            # 验证文件内容
            self.assertTrue(os.path.exists(temp_path), "文件应该已保存")
            with open(temp_path, 'r', encoding='utf-8') as f:
                content = f.read()
                self.assertIn("Test line", content)
        finally:
            if os.path.exists(temp_path):
                os.remove(temp_path)
    
    def test_close_file(self):
        """测试关闭文件功能"""
        with tempfile.NamedTemporaryFile(mode='w', delete=False, suffix='.txt') as f:
            temp_path = f.name
        
        try:
            self.workspace.load_file(temp_path)
            self.assertIsNotNone(self.workspace.get_active_editor())
            
            # 模拟用户输入'n'（不保存）
            import sys
            from io import StringIO
            old_stdin = sys.stdin
            sys.stdin = StringIO('n\n')
            
            try:
                self.workspace.close_file()
                self.assertIsNone(self.workspace.get_active_editor())
            finally:
                sys.stdin = old_stdin
        finally:
            if os.path.exists(temp_path):
                os.remove(temp_path)
    
    def test_switch_active_file(self):
        """测试切换活动文件功能"""
        with tempfile.NamedTemporaryFile(mode='w', delete=False, suffix='.txt') as f:
            temp_path1 = f.name
        
        with tempfile.NamedTemporaryFile(mode='w', delete=False, suffix='.txt') as f:
            temp_path2 = f.name
        
        try:
            self.workspace.load_file(temp_path1)
            self.workspace.load_file(temp_path2)
            
            self.assertEqual(self.workspace.get_active_file_path(), temp_path2)
            
            self.assertTrue(self.workspace.switch_active_file(temp_path1))
            self.assertEqual(self.workspace.get_active_file_path(), temp_path1)
        finally:
            if os.path.exists(temp_path1):
                os.remove(temp_path1)
            if os.path.exists(temp_path2):
                os.remove(temp_path2)
    
    def test_list_editors(self):
        """测试列出编辑器功能"""
        with tempfile.NamedTemporaryFile(mode='w', delete=False, suffix='.txt') as f:
            temp_path = f.name
        
        try:
            self.workspace.load_file(temp_path)
            editors = self.workspace.list_editors()
            
            self.assertEqual(len(editors), 1)
            file_path, is_modified, is_active = editors[0]
            self.assertEqual(file_path, temp_path)
            self.assertTrue(is_active)
        finally:
            if os.path.exists(temp_path):
                os.remove(temp_path)


if __name__ == '__main__':
    unittest.main()

