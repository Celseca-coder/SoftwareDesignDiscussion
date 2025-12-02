"""
工作区模块（Workspace）

管理当前会话的全局状态，包括已打开文件列表、当前活动文件、文件修改状态等。
使用备忘录模式实现状态持久化。
使用观察者模式实现事件通知。
"""

import os
import json
from typing import Dict, List, Optional
from text_editor import TextEditor
from command import Command
from logging_module import LoggingManager, Observer


class WorkspaceMemento:
    """
    工作区备忘录类（Memento Pattern）
    
    用于保存和恢复工作区的状态。
    只保存需要持久化的状态，不保存undo/redo历史等临时状态。
    """
    
    def __init__(self, opened_files: List[str], active_file: str, 
                 modified_files: List[str], logging_status: Dict[str, bool]):
        """
        构造函数
        
        参数：
        - opened_files: 已打开的文件列表
        - active_file: 当前活动文件
        - modified_files: 已修改的文件列表
        - logging_status: 日志开关状态（文件路径 -> 是否启用）
        """
        self.opened_files = opened_files
        self.active_file = active_file
        self.modified_files = modified_files
        self.logging_status = logging_status


class EditorWrapper:
    """
    编辑器包装类
    
    包装TextEditor，添加命令历史（用于undo/redo）和日志观察者。
    """
    
    def __init__(self, editor: TextEditor):
        """
        构造函数
        
        参数：
        - editor: 文本编辑器对象
        """
        self.editor = editor
        self.undo_stack: List[Command] = []  # 撤销栈
        self.redo_stack: List[Command] = []  # 重做栈
        self.observers: List[Observer] = []  # 观察者列表
    
    def execute_command(self, command: Command, log_command: str = None):
        """
        执行命令并记录到历史
        
        参数：
        - command: 命令对象
        - log_command: 用于日志的命令字符串
        """
        command.execute()
        self.undo_stack.append(command)
        self.redo_stack.clear()  # 执行新命令后，清空重做栈
        
        # 通知观察者
        if log_command:
            for observer in self.observers:
                observer.update('command_executed', log_command)
    
    def undo(self) -> bool:
        """
        撤销上一步操作
        
        返回：
        - True: 撤销成功
        - False: 没有可撤销的操作
        """
        if len(self.undo_stack) == 0:
            return False
        
        command = self.undo_stack.pop()
        command.undo()
        self.redo_stack.append(command)
        return True
    
    def redo(self) -> bool:
        """
        重做上一步撤销的操作
        
        返回：
        - True: 重做成功
        - False: 没有可重做的操作
        """
        if len(self.redo_stack) == 0:
            return False
        
        command = self.redo_stack.pop()
        command.redo()
        self.undo_stack.append(command)
        return True
    
    def add_observer(self, observer: Observer):
        """
        添加观察者
        
        参数：
        - observer: 观察者对象
        """
        if observer not in self.observers:
            self.observers.append(observer)
    
    def remove_observer(self, observer: Observer):
        """
        移除观察者
        
        参数：
        - observer: 观察者对象
        """
        if observer in self.observers:
            self.observers.remove(observer)


class Workspace:
    """
    工作区类
    
    管理所有打开的编辑器、当前活动编辑器、状态持久化等。
    """
    
    WORKSPACE_FILE = ".editor_workspace"
    
    def __init__(self):
        """构造函数"""
        self._editors: Dict[str, EditorWrapper] = {}  # 文件路径 -> EditorWrapper的映射
        self._active_file: Optional[str] = None  # 当前活动文件路径
        self._recent_files: List[str] = []  # 最近使用的文件列表（用于切换）
        self._logging_manager = LoggingManager()
        
        # 尝试加载工作区状态
        self._load_workspace()
    
    def _create_memento(self) -> WorkspaceMemento:
        """
        创建备忘录（保存当前状态）
        
        返回：
        - WorkspaceMemento对象
        """
        opened_files = list(self._editors.keys())
        modified_files = [
            path for path, wrapper in self._editors.items()
            if wrapper.editor.is_modified()
        ]
        logging_status = {
            path: self._logging_manager.get_logger(path).is_enabled()
            for path in opened_files
        }
        
        return WorkspaceMemento(
            opened_files=opened_files,
            active_file=self._active_file,
            modified_files=modified_files,
            logging_status=logging_status
        )
    
    def _restore_from_memento(self, memento: WorkspaceMemento):
        """
        从备忘录恢复状态
        
        参数：
        - memento: WorkspaceMemento对象
        """
        # 恢复打开的文件
        for file_path in memento.opened_files:
            if os.path.exists(file_path):
                editor = TextEditor(file_path)
                editor.load_from_file(file_path)
                wrapper = EditorWrapper(editor)
                self._editors[file_path] = wrapper
                
                # 检查文件第一行是否为# log
                if editor.get_line_count() > 0 and editor.get_line(1) == "# log":
                    self._logging_manager.enable_logging(file_path)
                    logger = self._logging_manager.get_logger(file_path)
                    wrapper.add_observer(logger)
        
        # 恢复活动文件
        if memento.active_file and memento.active_file in self._editors:
            self._active_file = memento.active_file
        
        # 恢复修改状态
        for file_path in memento.modified_files:
            if file_path in self._editors:
                self._editors[file_path].editor.set_modified(True)
        
        # 恢复日志状态
        for file_path, enabled in memento.logging_status.items():
            if file_path in self._editors:
                if enabled:
                    self._logging_manager.enable_logging(file_path)
                    logger = self._logging_manager.get_logger(file_path)
                    self._editors[file_path].add_observer(logger)
    
    def _save_workspace(self):
        """保存工作区状态到文件"""
        memento = self._create_memento()
        
        try:
            with open(self.WORKSPACE_FILE, 'w', encoding='utf-8') as f:
                json.dump({
                    'opened_files': memento.opened_files,
                    'active_file': memento.active_file,
                    'modified_files': memento.modified_files,
                    'logging_status': memento.logging_status
                }, f, ensure_ascii=False, indent=2)
        except IOError as e:
            print(f"警告: 无法保存工作区状态: {e}")
    
    def _load_workspace(self):
        """从文件加载工作区状态"""
        if not os.path.exists(self.WORKSPACE_FILE):
            return
        
        try:
            with open(self.WORKSPACE_FILE, 'r', encoding='utf-8') as f:
                data = json.load(f)
                memento = WorkspaceMemento(
                    opened_files=data.get('opened_files', []),
                    active_file=data.get('active_file'),
                    modified_files=data.get('modified_files', []),
                    logging_status=data.get('logging_status', {})
                )
                self._restore_from_memento(memento)
        except (IOError, json.JSONDecodeError) as e:
            print(f"警告: 无法加载工作区状态: {e}")
    
    def load_file(self, file_path: str) -> bool:
        """
        加载文件到工作区
        
        参数：
        - file_path: 文件路径
        
        返回：
        - True: 加载成功
        - False: 加载失败
        """
        # 如果文件已打开，直接切换为活动文件
        if file_path in self._editors:
            self._active_file = file_path
            if file_path not in self._recent_files:
                self._recent_files.append(file_path)
            return True
        
        # 创建新的编辑器
        editor = TextEditor(file_path)
        editor.load_from_file(file_path)
        wrapper = EditorWrapper(editor)
        self._editors[file_path] = wrapper
        
        # 检查文件第一行是否为# log
        if editor.get_line_count() > 0 and editor.get_line(1) == "# log":
            self._logging_manager.enable_logging(file_path)
            logger = self._logging_manager.get_logger(file_path)
            wrapper.add_observer(logger)
        
        # 设置为活动文件
        self._active_file = file_path
        if file_path not in self._recent_files:
            self._recent_files.append(file_path)
        
        return True
    
    def init_file(self, file_path: str, with_log: bool = False) -> bool:
        """
        创建新缓冲区文件
        
        参数：
        - file_path: 文件路径
        - with_log: 是否在第一行添加# log
        
        返回：
        - True: 创建成功
        - False: 创建失败（文件已存在）
        """
        if os.path.exists(file_path):
            return False
        
        if file_path in self._editors:
            return False
        
        # 创建新编辑器
        editor = TextEditor(file_path)
        wrapper = EditorWrapper(editor)
        
        if with_log:
            editor.append("# log")
            # 自动启用日志
            self._logging_manager.enable_logging(file_path)
            logger = self._logging_manager.get_logger(file_path)
            wrapper.add_observer(logger)
        
        self._editors[file_path] = wrapper
        self._active_file = file_path
        if file_path not in self._recent_files:
            self._recent_files.append(file_path)
        
        return True
    
    def save_file(self, file_path: str = None) -> bool:
        """
        保存文件
        
        参数：
        - file_path: 文件路径，None表示保存当前活动文件
        
        返回：
        - True: 保存成功
        - False: 保存失败
        """
        if file_path is None:
            file_path = self._active_file
        
        if file_path is None:
            return False
        
        if file_path not in self._editors:
            return False
        
        try:
            self._editors[file_path].editor.save_to_file(file_path)
            return True
        except Exception as e:
            print(f"错误: 保存文件失败: {e}")
            return False
    
    def save_all_files(self) -> bool:
        """
        保存所有文件
        
        返回：
        - True: 全部保存成功
        """
        success = True
        for file_path in self._editors.keys():
            if not self.save_file(file_path):
                success = False
        return success
    
    def close_file(self, file_path: str = None) -> bool:
        """
        关闭文件
        
        参数：
        - file_path: 文件路径，None表示关闭当前活动文件
        
        返回：
        - True: 关闭成功
        - False: 关闭失败
        """
        if file_path is None:
            file_path = self._active_file
        
        if file_path is None:
            return False
        
        if file_path not in self._editors:
            return False
        
        # 检查文件是否已修改
        wrapper = self._editors[file_path]
        if wrapper.editor.is_modified():
            response = input(f"文件已修改，是否保存? (y/n): ").strip().lower()
            if response == 'y':
                if not self.save_file(file_path):
                    return False
        
        # 移除观察者
        if file_path in self._logging_manager._loggers:
            logger = self._logging_manager._loggers[file_path]
            wrapper.remove_observer(logger)
        
        # 从工作区移除
        del self._editors[file_path]
        if file_path in self._recent_files:
            self._recent_files.remove(file_path)
        
        # 切换活动文件
        if self._active_file == file_path:
            if self._recent_files:
                self._active_file = self._recent_files[-1]
            else:
                self._active_file = None
        
        return True
    
    def switch_active_file(self, file_path: str) -> bool:
        """
        切换活动文件
        
        参数：
        - file_path: 文件路径
        
        返回：
        - True: 切换成功
        - False: 切换失败（文件未打开）
        """
        if file_path not in self._editors:
            return False
        
        self._active_file = file_path
        # 更新最近使用列表
        if file_path in self._recent_files:
            self._recent_files.remove(file_path)
        self._recent_files.append(file_path)
        return True
    
    def get_active_editor(self) -> Optional[EditorWrapper]:
        """
        获取当前活动编辑器
        
        返回：
        - EditorWrapper对象，如果没有活动文件则返回None
        """
        if self._active_file is None:
            return None
        return self._editors.get(self._active_file)
    
    def get_editor(self, file_path: str) -> Optional[EditorWrapper]:
        """
        获取指定文件的编辑器
        
        参数：
        - file_path: 文件路径
        
        返回：
        - EditorWrapper对象，如果文件未打开则返回None
        """
        return self._editors.get(file_path)
    
    def list_editors(self) -> List[tuple]:
        """
        列出所有打开的编辑器
        
        返回：
        - [(文件路径, 是否已修改, 是否为活动文件), ...]
        """
        result = []
        for file_path, wrapper in self._editors.items():
            is_active = (file_path == self._active_file)
            is_modified = wrapper.editor.is_modified()
            result.append((file_path, is_modified, is_active))
        return result
    
    def enable_logging(self, file_path: str = None) -> bool:
        """
        启用日志记录
        
        参数：
        - file_path: 文件路径，None表示当前活动文件
        
        返回：
        - True: 启用成功
        """
        if file_path is None:
            file_path = self._active_file
        
        if file_path is None or file_path not in self._editors:
            return False
        
        self._logging_manager.enable_logging(file_path)
        logger = self._logging_manager.get_logger(file_path)
        self._editors[file_path].add_observer(logger)
        return True
    
    def disable_logging(self, file_path: str = None) -> bool:
        """
        禁用日志记录
        
        参数：
        - file_path: 文件路径，None表示当前活动文件
        
        返回：
        - True: 禁用成功
        """
        if file_path is None:
            file_path = self._active_file
        
        if file_path is None or file_path not in self._editors:
            return False
        
        self._logging_manager.disable_logging(file_path)
        logger = self._logging_manager.get_logger(file_path)
        self._editors[file_path].remove_observer(logger)
        return True
    
    def get_log_content(self, file_path: str = None) -> str:
        """
        获取日志内容
        
        参数：
        - file_path: 文件路径，None表示当前活动文件
        
        返回：
        - 日志内容
        """
        if file_path is None:
            file_path = self._active_file
        
        if file_path is None:
            return "无活动文件"
        
        return self._logging_manager.get_log_content(file_path)
    
    def exit(self):
        """退出工作区，保存状态"""
        self._save_workspace()
    
    def get_active_file_path(self) -> Optional[str]:
        """获取当前活动文件路径"""
        return self._active_file

