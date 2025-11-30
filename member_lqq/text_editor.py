"""
文本编辑器模块（TextEditor）

这是核心的文本编辑器类，负责文本的存储和基本编辑操作。
使用行数组（List[str]）存储文本，每个元素是一行。
"""

from typing import List


class TextEditor:
    """
    文本编辑器类
    
    职责：
    - 存储文本内容（使用行数组）
    - 提供基本的编辑操作（追加、插入、删除、替换、显示）
    - 维护修改状态（modified标记）
    """
    
    def __init__(self, file_path: str = None):
        """
        构造函数
        
        参数：
        - file_path: 文件路径，用于标识这个编辑器对应的文件
        """
        self._file_path = file_path  # 文件路径
        self._lines: List[str] = []  # 文本内容，每行作为一个元素
        self._modified = False  # 修改标记，表示文件是否被修改过
    
    def get_file_path(self) -> str:
        """获取文件路径"""
        return self._file_path
    
    def set_file_path(self, file_path: str):
        """设置文件路径"""
        self._file_path = file_path
    
    def is_modified(self) -> bool:
        """检查文件是否被修改过"""
        return self._modified
    
    def set_modified(self, modified: bool):
        """设置修改标记"""
        self._modified = modified
    
    def get_lines(self) -> List[str]:
        """获取所有行的副本（避免外部直接修改）"""
        return self._lines.copy()
    
    def get_line_count(self) -> int:
        """获取总行数"""
        return len(self._lines)
    
    def get_line(self, line_num: int) -> str:
        """
        获取指定行的内容
        
        参数：
        - line_num: 行号（从1开始）
        
        返回：
        - 指定行的内容
        
        异常：
        - IndexError: 行号越界
        """
        if line_num < 1 or line_num > len(self._lines):
            raise IndexError(f"行号越界: {line_num}")
        return self._lines[line_num - 1]
    
    def append(self, text: str):
        """
        追加文本到文件末尾
        
        参数：
        - text: 要追加的文本（可以是多行，包含\\n）
        """
        # 如果文本包含换行符，需要拆分成多行
        if '\n' in text:
            lines = text.split('\n')
            self._lines.extend(lines)
        else:
            self._lines.append(text)
        self._modified = True  # 标记为已修改
    
    def insert(self, line: int, col: int, text: str):
        """
        在指定位置插入文本
        
        参数：
        - line: 行号（从1开始）
        - col: 列号（从1开始）
        - text: 要插入的文本（可以包含换行符）
        
        异常：
        - IndexError: 行号或列号越界
        - ValueError: 空文件不能在非1:1位置插入
        """
        # 检查是否是空文件
        if len(self._lines) == 0:
            if line == 1 and col == 1:
                # 空文件只能在1:1位置插入
                if '\n' in text:
                    self._lines = text.split('\n')
                else:
                    self._lines = [text]
                self._modified = True
                return
            else:
                raise ValueError("空文件只能在1:1位置插入")
        
        # 检查行号是否越界
        if line < 1 or line > len(self._lines):
            raise IndexError(f"行号越界: {line}")
        
        # 检查列号是否越界
        current_line = self._lines[line - 1]
        if col < 1 or col > len(current_line) + 1:
            raise IndexError(f"列号越界: {col}")
        
        # 如果文本包含换行符，需要拆分成多行
        if '\n' in text:
            lines = text.split('\n')
            # 将当前行分割成两部分
            before = current_line[:col - 1]
            after = current_line[col - 1:]
            
            # 重新组织行
            new_lines = []
            new_lines.extend(self._lines[:line - 1])  # 前面的行
            new_lines.append(before + lines[0])  # 第一行
            new_lines.extend(lines[1:])  # 中间的行
            new_lines.append(lines[-1] + after)  # 最后一行
            new_lines.extend(self._lines[line:])  # 后面的行
            
            self._lines = new_lines
        else:
            # 单行插入
            # 将指定位置插入文本
            new_line = current_line[:col - 1] + text + current_line[col - 1:]
            self._lines[line - 1] = new_line
        
        self._modified = True
    
    def delete(self, line: int, col: int, length: int):
        """
        从指定位置删除指定长度的字符
        
        参数：
        - line: 行号（从1开始）
        - col: 列号（从1开始）
        - length: 要删除的字符数
        
        异常：
        - IndexError: 行号或列号越界
        - ValueError: 删除长度超出行尾
        """
        # 检查行号是否越界
        if line < 1 or line > len(self._lines):
            raise IndexError(f"行号越界: {line}")
        
        current_line = self._lines[line - 1]
        
        # 检查列号是否越界
        if col < 1 or col > len(current_line):
            raise IndexError(f"列号越界: {col}")
        
        # 检查删除长度是否超出该行剩余字符数
        remaining_chars = len(current_line) - col + 1
        if length > remaining_chars:
            raise ValueError(f"删除长度超出行尾: 剩余{remaining_chars}个字符，尝试删除{length}个")
        
        # 执行删除操作
        new_line = current_line[:col - 1] + current_line[col - 1 + length:]
        self._lines[line - 1] = new_line
        self._modified = True
    
    def replace(self, line: int, col: int, length: int, text: str):
        """
        替换指定位置的文本
        
        参数：
        - line: 行号（从1开始）
        - col: 列号（从1开始）
        - length: 要替换的字符数
        - text: 替换后的文本（可以为空，相当于删除）
        
        注意：这个方法实际上等效于先delete再insert，但为了保持行的完整性，
        我们直接在单行内处理，不支持跨行替换。
        """
        # 检查行号是否越界
        if line < 1 or line > len(self._lines):
            raise IndexError(f"行号越界: {line}")
        
        current_line = self._lines[line - 1]
        
        # 检查列号是否越界
        if col < 1 or col > len(current_line) + 1:
            raise IndexError(f"列号越界: {col}")
        
        # 检查删除长度（如果length为0，只是插入）
        if length > 0:
            remaining_chars = len(current_line) - col + 1
            if length > remaining_chars:
                raise ValueError(f"替换长度超出行尾: 剩余{remaining_chars}个字符，尝试替换{length}个")
        
        # 执行替换操作
        # 先删除指定长度的字符，再插入新文本
        new_line = current_line[:col - 1] + text + current_line[col - 1 + length:]
        
        # 如果替换的文本包含换行符，需要拆分成多行
        if '\n' in new_line:
            lines = new_line.split('\n')
            new_lines = []
            new_lines.extend(self._lines[:line - 1])  # 前面的行
            new_lines.extend(lines)  # 拆分后的行
            new_lines.extend(self._lines[line:])  # 后面的行
            self._lines = new_lines
        else:
            # 不包含换行符，直接替换
            self._lines[line - 1] = new_line
        
        self._modified = True
    
    def show(self, start_line: int = None, end_line: int = None) -> str:
        """
        显示指定范围的内容
        
        参数：
        - start_line: 起始行号（从1开始），None表示从第一行开始
        - end_line: 结束行号（包含），None表示到最后一行
        
        返回：
        - 格式化的字符串，每行前面有行号
        """
        if len(self._lines) == 0:
            return ""
        
        # 如果没有指定范围，显示全部
        if start_line is None:
            start_line = 1
        if end_line is None:
            end_line = len(self._lines)
        
        # 边界检查
        if start_line < 1:
            start_line = 1
        if end_line > len(self._lines):
            end_line = len(self._lines)
        if start_line > end_line:
            return ""
        
        # 格式化输出
        result = []
        for i in range(start_line, end_line + 1):
            result.append(f"{i}: {self._lines[i - 1]}")
        
        return "\n".join(result)
    
    def load_from_file(self, file_path: str):
        """
        从文件加载内容
        
        参数：
        - file_path: 文件路径
        """
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
                # 按行分割（保留空行）
                if content:
                    # 如果文件以换行符结尾，split会多一个空字符串
                    self._lines = content.split('\n')
                    # 移除最后的空行（如果文件以换行符结尾）
                    if content.endswith('\n') and len(self._lines) > 0 and self._lines[-1] == '':
                        self._lines.pop()
                else:
                    self._lines = []
            self._file_path = file_path
            self._modified = False
        except FileNotFoundError:
            # 文件不存在，创建新文件
            self._lines = []
            self._file_path = file_path
            self._modified = True
    
    def save_to_file(self, file_path: str = None):
        """
        保存内容到文件
        
        参数：
        - file_path: 文件路径，如果为None则使用当前文件路径
        """
        if file_path is None:
            file_path = self._file_path
        
        if file_path is None:
            raise ValueError("文件路径未指定")
        
        # 将行数组用换行符连接
        content = '\n'.join(self._lines)
        
        try:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            self._modified = False
            self._file_path = file_path
        except IOError as e:
            raise IOError(f"无法写入文件: {e}")
    
    def get_content(self) -> str:
        """
        获取完整文本内容（用于保存）
        
        返回：
        - 完整的文本内容（行之间用\\n连接）
        """
        return '\n'.join(self._lines)
    
    def set_content(self, content: str):
        """
        设置完整文本内容
        
        参数：
        - content: 完整的文本内容
        """
        if content:
            self._lines = content.split('\n')
            # 如果内容以换行符结尾，移除最后的空行
            if content.endswith('\n') and len(self._lines) > 0 and self._lines[-1] == '':
                self._lines.pop()
        else:
            self._lines = []
        self._modified = True

