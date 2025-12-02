"""
命令解析器模块（Command Parser）

负责解析用户输入的命令，将字符串命令转换为可执行的操作。
"""

import re
from typing import List, Tuple, Optional


class CommandParser:
    """
    命令解析器类
    
    解析用户输入的命令字符串，提取命令名和参数。
    """
    
    @staticmethod
    def parse(command_line: str) -> Tuple[str, List[str]]:
        """
        解析命令字符串
        
        参数：
        - command_line: 用户输入的命令字符串
        
        返回：
        - (命令名, 参数列表) 元组
        
        示例：
        - "load test.txt" -> ("load", ["test.txt"])
        - 'append "hello world"' -> ("append", ["hello world"])
        - 'insert 1:5 "text"' -> ("insert", ["1:5", "text"])
        """
        command_line = command_line.strip()
        if not command_line:
            return ("", [])
        
        # 分割命令和参数
        parts = CommandParser._split_command(command_line)
        if not parts:
            return ("", [])
        
        command_name = parts[0]
        args = parts[1:] if len(parts) > 1 else []
        
        return (command_name, args)
    
    @staticmethod
    def _split_command(command_line: str) -> List[str]:
        """
        分割命令字符串
        
        处理带引号的参数（引号内的空格不作为分隔符）
        
        参数：
        - command_line: 命令字符串
        
        返回：
        - 分割后的部分列表
        """
        parts = []
        current = ""
        in_quotes = False
        i = 0
        
        while i < len(command_line):
            char = command_line[i]
            
            if char == '"':
                # 遇到引号
                if in_quotes:
                    # 结束引号
                    in_quotes = False
                    if current:
                        parts.append(current)
                        current = ""
                else:
                    # 开始引号
                    if current.strip():
                        parts.append(current.strip())
                        current = ""
                    in_quotes = True
            elif char == ' ' and not in_quotes:
                # 空格且不在引号内，作为分隔符
                if current.strip():
                    parts.append(current.strip())
                    current = ""
            else:
                # 普通字符
                current += char
            
            i += 1
        
        # 处理最后的部分
        if current.strip():
            parts.append(current.strip())
        
        return parts
    
    @staticmethod
    def parse_line_col(param: str) -> Tuple[int, int]:
        """
        解析行号:列号格式的参数
        
        参数：
        - param: 格式为 "line:col" 的字符串
        
        返回：
        - (行号, 列号) 元组
        
        异常：
        - ValueError: 格式错误
        """
        if ':' not in param:
            raise ValueError(f"无效的行号:列号格式: {param}")
        
        parts = param.split(':')
        if len(parts) != 2:
            raise ValueError(f"无效的行号:列号格式: {param}")
        
        try:
            line = int(parts[0])
            col = int(parts[1])
            return (line, col)
        except ValueError:
            raise ValueError(f"行号或列号必须是数字: {param}")
    
    @staticmethod
    def parse_range(param: str) -> Tuple[int, int]:
        """
        解析范围参数（start:end格式）
        
        参数：
        - param: 格式为 "start:end" 的字符串
        
        返回：
        - (起始行号, 结束行号) 元组，如果只有一个数字则返回(start, start)
        
        异常：
        - ValueError: 格式错误
        """
        if ':' not in param:
            # 只有一个数字，表示单行
            try:
                line = int(param)
                return (line, line)
            except ValueError:
                raise ValueError(f"无效的行号: {param}")
        
        parts = param.split(':')
        if len(parts) != 2:
            raise ValueError(f"无效的范围格式: {param}")
        
        try:
            start = int(parts[0])
            end = int(parts[1])
            return (start, end)
        except ValueError:
            raise ValueError(f"起始行号或结束行号必须是数字: {param}")


class CommandExecutor:
    """
    命令执行器类
    
    负责执行解析后的命令。
    """
    
    def __init__(self, workspace):
        """
        构造函数
        
        参数：
        - workspace: Workspace对象
        """
        self.workspace = workspace
        self.parser = CommandParser()
    
    def execute(self, command_line: str) -> str:
        """
        执行命令
        
        参数：
        - command_line: 命令字符串
        
        返回：
        - 执行结果消息
        """
        command_name, args = self.parser.parse(command_line)
        
        if not command_name:
            return ""
        
        # 根据命令名分发到对应的处理方法
        method_name = f"_handle_{command_name}"
        if hasattr(self, method_name):
            try:
                method = getattr(self, method_name)
                return method(args)
            except Exception as e:
                return f"错误: {str(e)}"
        else:
            return f"未知命令: {command_name}"
    
    # ========== 工作区命令 ==========
    
    def _handle_load(self, args: List[str]) -> str:
        """处理load命令"""
        if len(args) < 1:
            return "错误: load命令需要文件路径参数"
        
        file_path = args[0]
        if self.workspace.load_file(file_path):
            # 记录日志（如果文件启用了日志）
            editor_wrapper = self.workspace.get_editor(file_path)
            if editor_wrapper:
                log_command = f"load {file_path}"
                for observer in editor_wrapper.observers:
                    observer.update('command_executed', log_command)
            return f"已加载文件: {file_path}"
        else:
            return f"错误: 无法加载文件 {file_path}"
    
    def _handle_save(self, args: List[str]) -> str:
        """处理save命令"""
        if len(args) == 0:
            # 保存当前活动文件
            file_path = self.workspace.get_active_file_path()
            if self.workspace.save_file():
                # 记录日志（如果文件启用了日志）
                if file_path:
                    editor_wrapper = self.workspace.get_editor(file_path)
                    if editor_wrapper:
                        log_command = "save"
                        for observer in editor_wrapper.observers:
                            observer.update('command_executed', log_command)
                return "文件已保存"
            else:
                return "错误: 保存失败"
        elif args[0] == "all":
            # 保存所有文件
            if self.workspace.save_all_files():
                # 记录所有已保存文件的日志
                editors = self.workspace.list_editors()
                for file_path, _, _ in editors:
                    editor_wrapper = self.workspace.get_editor(file_path)
                    if editor_wrapper:
                        log_command = "save all"
                        for observer in editor_wrapper.observers:
                            observer.update('command_executed', log_command)
                return "所有文件已保存"
            else:
                return "错误: 部分文件保存失败"
        else:
            # 保存指定文件
            file_path = args[0]
            if self.workspace.save_file(file_path):
                # 记录日志（如果文件启用了日志）
                editor_wrapper = self.workspace.get_editor(file_path)
                if editor_wrapper:
                    log_command = f"save {file_path}"
                    for observer in editor_wrapper.observers:
                        observer.update('command_executed', log_command)
                return f"文件已保存: {file_path}"
            else:
                return f"错误: 无法保存文件 {file_path}"
    
    def _handle_init(self, args: List[str]) -> str:
        """处理init命令"""
        if len(args) < 1:
            return "错误: init命令需要文件路径参数"
        
        file_path = args[0]
        with_log = len(args) > 1 and args[1] == "with-log"
        
        if self.workspace.init_file(file_path, with_log):
            # 记录日志（如果文件启用了日志）
            editor_wrapper = self.workspace.get_editor(file_path)
            if editor_wrapper:
                log_command = f"init {file_path}" + (" with-log" if with_log else "")
                for observer in editor_wrapper.observers:
                    observer.update('command_executed', log_command)
            return f"已创建新缓冲区: {file_path}"
        else:
            return f"错误: 文件已存在或无法创建: {file_path}"
    
    def _handle_close(self, args: List[str]) -> str:
        """处理close命令"""
        file_path = args[0] if len(args) > 0 else None
        if file_path is None:
            file_path = self.workspace.get_active_file_path()
        
        # 在关闭前记录日志
        if file_path:
            editor_wrapper = self.workspace.get_editor(file_path)
            if editor_wrapper:
                log_command = f"close {file_path}" if file_path else "close"
                for observer in editor_wrapper.observers:
                    observer.update('command_executed', log_command)
        
        if self.workspace.close_file(file_path):
            return "文件已关闭"
        else:
            return "错误: 关闭文件失败"
    
    def _handle_edit(self, args: List[str]) -> str:
        """处理edit命令"""
        if len(args) < 1:
            return "错误: edit命令需要文件路径参数"
        
        file_path = args[0]
        if self.workspace.switch_active_file(file_path):
            return f"已切换到文件: {file_path}"
        else:
            return f"错误: 文件未打开: {file_path}"
    
    def _handle_editor_list(self, args: List[str]) -> str:
        """处理editor-list命令"""
        editors = self.workspace.list_editors()
        if not editors:
            return "没有打开的编辑器"
        
        result = []
        for file_path, is_modified, is_active in editors:
            status = ""
            if is_active:
                status += "> "
            status += file_path
            if is_modified:
                status += "*"
            result.append(status)
        
        return "\n".join(result)
    
    def _handle_dir_tree(self, args: List[str]) -> str:
        """处理dir-tree命令"""
        import os
        
        path = args[0] if len(args) > 0 else "."
        
        if not os.path.exists(path):
            return f"错误: 路径不存在: {path}"
        
        if not os.path.isdir(path):
            return f"错误: 不是目录: {path}"
        
        return self._build_tree(path, "", True)
    
    def _build_tree(self, path: str, prefix: str, is_last: bool) -> str:
        """
        构建目录树字符串
        
        参数：
        - path: 当前路径
        - prefix: 前缀字符串
        - is_last: 是否是最后一个
        
        返回：
        - 目录树字符串
        """
        import os
        
        result = []
        name = os.path.basename(path) if path != "." else os.path.basename(os.getcwd())
        
        # 添加当前目录/文件
        if path == ".":
            result.append(name)
        else:
            result.append(prefix + ("└── " if is_last else "├── ") + name)
        
        # 如果是目录，递归处理子项
        if os.path.isdir(path):
            try:
                items = sorted(os.listdir(path))
                # 过滤掉隐藏文件（以.开头）和.editor_workspace
                items = [item for item in items if not item.startswith('.')]
                
                for i, item in enumerate(items):
                    item_path = os.path.join(path, item)
                    is_last_item = (i == len(items) - 1)
                    new_prefix = prefix + ("    " if is_last else "│   ")
                    result.append(self._build_tree(item_path, new_prefix, is_last_item))
            except PermissionError:
                pass
        
        return "\n".join(result)
    
    def _handle_undo(self, args: List[str]) -> str:
        """处理undo命令"""
        editor = self.workspace.get_active_editor()
        if editor is None:
            return "错误: 没有活动文件"
        
        if editor.undo():
            return "已撤销"
        else:
            return "没有可撤销的操作"
    
    def _handle_redo(self, args: List[str]) -> str:
        """处理redo命令"""
        editor = self.workspace.get_active_editor()
        if editor is None:
            return "错误: 没有活动文件"
        
        if editor.redo():
            return "已重做"
        else:
            return "没有可重做的操作"
    
    def _handle_exit(self, args: List[str]) -> str:
        """处理exit命令"""
        # 检查是否有未保存的文件
        editors = self.workspace.list_editors()
        modified_files = [path for path, modified, _ in editors if modified]
        
        if modified_files:
            result = "以下文件已修改:\n"
            for path in modified_files:
                result += f"  {path}\n"
            result += "是否保存所有文件? (y/n): "
            print(result, end="")
            response = input().strip().lower()
            if response == 'y':
                self.workspace.save_all_files()
        
        self.workspace.exit()
        return "exit"  # 特殊返回值，表示退出程序
    
    # ========== 文本编辑命令 ==========
    
    def _handle_append(self, args: List[str]) -> str:
        """处理append命令"""
        if len(args) < 1:
            return "错误: append命令需要文本参数"
        
        editor_wrapper = self.workspace.get_active_editor()
        if editor_wrapper is None:
            return "错误: 没有活动文件"
        
        text = args[0]
        
        # 创建并执行命令
        from command import AppendCommand
        command = AppendCommand(editor_wrapper.editor, text)
        
        # 构建日志命令字符串
        log_command = f'append "{text}"'
        editor_wrapper.execute_command(command, log_command)
        
        return "文本已追加"
    
    def _handle_insert(self, args: List[str]) -> str:
        """处理insert命令"""
        if len(args) < 2:
            return "错误: insert命令需要行号:列号和文本参数"
        
        editor_wrapper = self.workspace.get_active_editor()
        if editor_wrapper is None:
            return "错误: 没有活动文件"
        
        try:
            line, col = self.parser.parse_line_col(args[0])
        except ValueError as e:
            return f"错误: {str(e)}"
        
        text = args[1]
        
        # 创建并执行命令
        from command import InsertCommand
        command = InsertCommand(editor_wrapper.editor, line, col, text)
        
        # 构建日志命令字符串
        log_command = f'insert {line}:{col} "{text}"'
        editor_wrapper.execute_command(command, log_command)
        
        return "文本已插入"
    
    def _handle_delete(self, args: List[str]) -> str:
        """处理delete命令"""
        if len(args) < 2:
            return "错误: delete命令需要行号:列号和长度参数"
        
        editor_wrapper = self.workspace.get_active_editor()
        if editor_wrapper is None:
            return "错误: 没有活动文件"
        
        try:
            line, col = self.parser.parse_line_col(args[0])
            length = int(args[1])
        except ValueError as e:
            return f"错误: {str(e)}"
        
        # 创建并执行命令
        from command import DeleteCommand
        command = DeleteCommand(editor_wrapper.editor, line, col, length)
        
        # 构建日志命令字符串
        log_command = f'delete {line}:{col} {length}'
        editor_wrapper.execute_command(command, log_command)
        
        return "文本已删除"
    
    def _handle_replace(self, args: List[str]) -> str:
        """处理replace命令"""
        if len(args) < 3:
            return "错误: replace命令需要行号:列号、长度和文本参数"
        
        editor_wrapper = self.workspace.get_active_editor()
        if editor_wrapper is None:
            return "错误: 没有活动文件"
        
        try:
            line, col = self.parser.parse_line_col(args[0])
            length = int(args[1])
        except ValueError as e:
            return f"错误: {str(e)}"
        
        text = args[2]
        
        # 创建并执行命令
        from command import ReplaceCommand
        command = ReplaceCommand(editor_wrapper.editor, line, col, length, text)
        
        # 构建日志命令字符串
        log_command = f'replace {line}:{col} {length} "{text}"'
        editor_wrapper.execute_command(command, log_command)
        
        return "文本已替换"
    
    def _handle_show(self, args: List[str]) -> str:
        """处理show命令"""
        editor_wrapper = self.workspace.get_active_editor()
        if editor_wrapper is None:
            return "错误: 没有活动文件"
        
        editor = editor_wrapper.editor
        
        if len(args) == 0:
            # 显示全部
            return editor.show()
        else:
            # 显示指定范围
            try:
                start, end = self.parser.parse_range(args[0])
                return editor.show(start, end)
            except ValueError as e:
                return f"错误: {str(e)}"
    
    # ========== 日志命令 ==========
    
    def _handle_log_on(self, args: List[str]) -> str:
        """处理log-on命令"""
        file_path = args[0] if len(args) > 0 else None
        
        if self.workspace.enable_logging(file_path):
            target = file_path if file_path else "当前活动文件"
            return f"已启用日志记录: {target}"
        else:
            return "错误: 启用日志记录失败"
    
    def _handle_log_off(self, args: List[str]) -> str:
        """处理log-off命令"""
        file_path = args[0] if len(args) > 0 else None
        
        if self.workspace.disable_logging(file_path):
            target = file_path if file_path else "当前活动文件"
            return f"已关闭日志记录: {target}"
        else:
            return "错误: 关闭日志记录失败"
    
    def _handle_log_show(self, args: List[str]) -> str:
        """处理log-show命令"""
        file_path = args[0] if len(args) > 0 else None
        log_content = self.workspace.get_log_content(file_path)
        return log_content

