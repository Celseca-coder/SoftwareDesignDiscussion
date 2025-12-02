"""
日志模块（Logging Module）

使用观察者模式实现日志记录功能。
当日志开关打开时，记录所有命令的执行。
"""

import os
from datetime import datetime
from typing import List
from abc import ABC, abstractmethod


class Observer(ABC):
    """
    观察者接口
    
    观察者模式中的观察者接口，所有需要监听事件的类都应该实现这个接口。
    """
    
    @abstractmethod
    def update(self, event_type: str, *args, **kwargs):
        """
        更新方法，当被观察对象发生事件时调用
        
        参数：
        - event_type: 事件类型（如'command_executed'）
        - *args: 位置参数
        - **kwargs: 关键字参数
        """
        pass


class Logger(Observer):
    """
    日志记录器
    
    实现观察者接口，用于记录命令执行日志。
    """
    
    def __init__(self, file_path: str):
        """
        构造函数
        
        参数：
        - file_path: 关联的文件路径，用于确定日志文件位置
        """
        self._file_path = file_path
        self._log_file_path = self._get_log_file_path(file_path)
        self._enabled = False  # 日志开关
        self._session_start_time = None  # 会话开始时间
    
    def _get_log_file_path(self, file_path: str) -> str:
        """
        根据文件路径生成日志文件路径
        
        参数：
        - file_path: 原文件路径
        
        返回：
        - 日志文件路径（.filename.log格式）
        """
        if not file_path:
            return None
        
        # 获取文件所在目录和文件名
        directory = os.path.dirname(file_path)
        filename = os.path.basename(file_path)
        
        # 生成日志文件名：.filename.log
        log_filename = f".{filename}.log"
        
        # 如果是绝对路径，直接拼接
        if os.path.isabs(file_path):
            return os.path.join(directory, log_filename)
        else:
            # 相对路径
            if directory:
                return os.path.join(directory, log_filename)
            else:
                return log_filename
    
    def enable(self):
        """启用日志记录"""
        self._enabled = True
        # 如果这是第一次启用，记录会话开始时间
        if self._session_start_time is None:
            self._session_start_time = datetime.now()
            self._write_session_start()
    
    def disable(self):
        """禁用日志记录"""
        self._enabled = False
    
    def is_enabled(self) -> bool:
        """检查日志是否启用"""
        return self._enabled
    
    def _write_session_start(self):
        """写入会话开始标记"""
        if not self._log_file_path:
            return
        
        try:
            with open(self._log_file_path, 'a', encoding='utf-8') as f:
                timestamp = self._session_start_time.strftime("%Y%m%d %H:%M:%S")
                f.write(f"session start at {timestamp}\n")
        except IOError as e:
            print(f"警告: 无法写入日志文件: {e}")
    
    def _write_log(self, command: str):
        """
        写入日志条目
        
        参数：
        - command: 命令字符串
        """
        if not self._enabled or not self._log_file_path:
            return
        
        try:
            with open(self._log_file_path, 'a', encoding='utf-8') as f:
                timestamp = datetime.now().strftime("%Y%m%d %H:%M:%S")
                f.write(f"{timestamp} {command}\n")
        except IOError as e:
            # 日志记录失败仅提示警告，不中断程序
            print(f"警告: 无法写入日志文件: {e}")
    
    def update(self, event_type: str, *args, **kwargs):
        """
        观察者更新方法
        
        参数：
        - event_type: 事件类型
        - *args: 位置参数（第一个参数通常是命令字符串）
        - **kwargs: 关键字参数
        """
        if event_type == 'command_executed':
            if args:
                command = args[0]
                self._write_log(command)
    
    def get_log_content(self) -> str:
        """
        获取日志文件内容
        
        返回：
        - 日志文件的内容
        """
        if not self._log_file_path or not os.path.exists(self._log_file_path):
            return "日志文件不存在"
        
        try:
            with open(self._log_file_path, 'r', encoding='utf-8') as f:
                return f.read()
        except IOError as e:
            return f"无法读取日志文件: {e}"


class LoggingManager:
    """
    日志管理器
    
    管理所有文件的日志记录器。
    """
    
    def __init__(self):
        """构造函数"""
        self._loggers: dict = {}  # 文件路径 -> Logger对象的映射
    
    def get_logger(self, file_path: str) -> Logger:
        """
        获取或创建日志记录器
        
        参数：
        - file_path: 文件路径
        
        返回：
        - Logger对象
        """
        if file_path not in self._loggers:
            self._loggers[file_path] = Logger(file_path)
        return self._loggers[file_path]
    
    def enable_logging(self, file_path: str):
        """
        启用指定文件的日志记录
        
        参数：
        - file_path: 文件路径
        """
        logger = self.get_logger(file_path)
        logger.enable()
    
    def disable_logging(self, file_path: str):
        """
        禁用指定文件的日志记录
        
        参数：
        - file_path: 文件路径
        """
        if file_path in self._loggers:
            self._loggers[file_path].disable()
    
    def get_log_content(self, file_path: str) -> str:
        """
        获取指定文件的日志内容
        
        参数：
        - file_path: 文件路径
        
        返回：
        - 日志内容
        """
        if file_path in self._loggers:
            return self._loggers[file_path].get_log_content()
        else:
            logger = Logger(file_path)
            return logger.get_log_content()

