"""
主程序入口

这是文本编辑器的主程序，负责启动编辑器并处理用户输入。
"""

from workspace import Workspace
from command_parser import CommandExecutor


def main():
    """
    主函数
    
    创建工作区和命令执行器，进入命令循环。
    """
    print("=" * 50)
    print("基于字符命令界面的文本编辑器")
    print("=" * 50)
    print("输入 'exit' 退出程序")
    print("输入 'help' 查看帮助")
    print()
    
    # 创建工作区和命令执行器
    workspace = Workspace()
    executor = CommandExecutor(workspace)
    
    # 主循环
    while True:
        try:
            # 显示提示符
            active_file = workspace.get_active_file_path()
            if active_file:
                prompt = f"[{active_file}]> "
            else:
                prompt = "> "
            
            # 获取用户输入
            command_line = input(prompt).strip()
            
            if not command_line:
                continue
            
            # 处理help命令
            if command_line == "help":
                print_help()
                continue
            
            # 执行命令
            result = executor.execute(command_line)
            
            # 检查是否是退出命令
            if result == "exit":
                print("再见！")
                break
            
            # 显示执行结果
            if result:
                print(result)
        
        except KeyboardInterrupt:
            # 处理Ctrl+C
            print("\n\n程序被中断。输入 'exit' 退出。")
        except EOFError:
            # 处理Ctrl+D（Windows下可能是Ctrl+Z）
            print("\n\n程序被中断。输入 'exit' 退出。")
        except Exception as e:
            print(f"错误: {str(e)}")


def print_help():
    """打印帮助信息"""
    help_text = """
可用命令：

工作区命令：
  load <file>                    - 加载文件
  save [file|all]                - 保存文件（不指定参数保存当前文件，all保存所有文件）
  init <file> [with-log]         - 创建新缓冲区（可选添加# log）
  close [file]                   - 关闭文件（不指定参数关闭当前文件）
  edit <file>                    - 切换活动文件
  editor-list                    - 显示文件列表
  dir-tree [path]                - 显示目录树（不指定参数显示当前目录）
  undo                           - 撤销上一步操作
  redo                           - 重做上一步撤销的操作
  exit                           - 退出程序

文本编辑命令：
  append "text"                  - 在文件末尾追加文本
  insert <line:col> "text"       - 在指定位置插入文本
  delete <line:col> <len>        - 删除指定位置的字符
  replace <line:col> <len> "text" - 替换指定位置的文本
  show [start:end]               - 显示文件内容（不指定参数显示全部）

日志命令：
  log-on [file]                  - 启用日志记录（不指定参数对当前文件启用）
  log-off [file]                 - 关闭日志记录（不指定参数对当前文件关闭）
  log-show [file]                - 显示日志内容（不指定参数显示当前文件的日志）

示例：
  load test.txt                  - 加载test.txt文件
  append "Hello World"           - 追加文本
  insert 1:1 "Start: "           - 在第1行第1列插入文本
  delete 1:1 5                   - 删除第1行第1列开始的5个字符
  replace 1:1 5 "Hi"             - 替换第1行第1列开始的5个字符
  show 1:10                      - 显示第1到10行
  init new.txt with-log          - 创建新文件并启用日志
"""
    print(help_text)


if __name__ == "__main__":
    main()

