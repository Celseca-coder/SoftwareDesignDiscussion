"""
命令模式实现（Command Pattern）

这个模块实现了命令模式，用于支持undo/redo功能。
每个编辑操作都会被封装成一个命令对象，可以撤销和重做。
"""

from abc import ABC, abstractmethod
from text_editor import TextEditor


class Command(ABC):
    """
    命令接口（抽象基类）
    
    所有命令都需要实现execute（执行）、undo（撤销）、redo（重做）方法。
    """
    
    @abstractmethod
    def execute(self):
        """执行命令"""
        pass
    
    @abstractmethod
    def undo(self):
        """撤销命令"""
        pass
    
    @abstractmethod
    def redo(self):
        """重做命令"""
        pass


class AppendCommand(Command):
    """追加命令"""
    
    def __init__(self, editor: TextEditor, text: str):
        """
        构造函数
        
        参数：
        - editor: 文本编辑器对象
        - text: 要追加的文本
        """
        self._editor = editor
        self._text = text
        self._original_line_count = 0  # 记录原始行数，用于撤销
    
    def execute(self):
        """执行追加操作"""
        self._original_line_count = self._editor.get_line_count()
        self._editor.append(self._text)
    
    def undo(self):
        """撤销追加操作"""
        # 删除追加的行
        current_lines = self._editor.get_lines()
        original_lines = current_lines[:self._original_line_count]
        self._editor.set_content('\n'.join(original_lines))
        self._editor.set_modified(True)
    
    def redo(self):
        """重做追加操作"""
        self._editor.append(self._text)


class InsertCommand(Command):
    """插入命令"""
    
    def __init__(self, editor: TextEditor, line: int, col: int, text: str):
        """
        构造函数
        
        参数：
        - editor: 文本编辑器对象
        - line: 行号
        - col: 列号
        - text: 要插入的文本
        """
        self._editor = editor
        self._line = line
        self._col = col
        self._text = text
        self._original_content = ""  # 保存原始内容，用于撤销
    
    def execute(self):
        """执行插入操作"""
        self._original_content = self._editor.get_content()
        self._editor.insert(self._line, self._col, self._text)
    
    def undo(self):
        """撤销插入操作"""
        self._editor.set_content(self._original_content)
        self._editor.set_modified(True)
    
    def redo(self):
        """重做插入操作"""
        self._editor.insert(self._line, self._col, self._text)


class DeleteCommand(Command):
    """删除命令"""
    
    def __init__(self, editor: TextEditor, line: int, col: int, length: int):
        """
        构造函数
        
        参数：
        - editor: 文本编辑器对象
        - line: 行号
        - col: 列号
        - length: 删除长度
        """
        self._editor = editor
        self._line = line
        self._col = col
        self._length = length
        self._deleted_text = ""  # 保存被删除的文本，用于撤销
    
    def execute(self):
        """执行删除操作"""
        # 保存被删除的文本
        line_content = self._editor.get_line(self._line)
        start_idx = self._col - 1
        end_idx = min(start_idx + self._length, len(line_content))
        self._deleted_text = line_content[start_idx:end_idx]
        
        self._editor.delete(self._line, self._col, self._length)
    
    def undo(self):
        """撤销删除操作"""
        # 重新插入被删除的文本
        self._editor.insert(self._line, self._col, self._deleted_text)
    
    def redo(self):
        """重做删除操作"""
        self._editor.delete(self._line, self._col, self._length)


class ReplaceCommand(Command):
    """替换命令"""
    
    def __init__(self, editor: TextEditor, line: int, col: int, length: int, text: str):
        """
        构造函数
        
        参数：
        - editor: 文本编辑器对象
        - line: 行号
        - col: 列号
        - length: 替换长度
        - text: 替换后的文本
        """
        self._editor = editor
        self._line = line
        self._col = col
        self._length = length
        self._text = text
        self._original_content = ""  # 保存原始内容，用于撤销
    
    def execute(self):
        """执行替换操作"""
        self._original_content = self._editor.get_content()
        self._editor.replace(self._line, self._col, self._length, self._text)
    
    def undo(self):
        """撤销替换操作"""
        self._editor.set_content(self._original_content)
        self._editor.set_modified(True)
    
    def redo(self):
        """重做替换操作"""
        self._editor.replace(self._line, self._col, self._length, self._text)

