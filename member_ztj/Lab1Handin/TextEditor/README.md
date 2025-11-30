# Java 文本编辑器

一个基于命令模式和观察者模式设计的命令行文本编辑器。

## 特性

- **命令模式**: 支持完整的撤销/重做功能
- **观察者模式**: 日志系统自动记录操作
- **备忘录模式**: 工作区状态持久化
- **多文件管理**: 同时打开和编辑多个文件
- **交互式CLI**: REPL式命令行界面

## 设计模式

1. **命令模式 (Command Pattern)**: 所有编辑操作封装为命令对象
2. **观察者模式 (Observer Pattern)**: 日志系统监听系统事件
3. **备忘录模式 (Memento Pattern)**: 工作区状态快照和恢复
4. **单例模式 (Singleton Pattern)**: 日志管理器全局唯一实例

## 项目结构

```
text-editor/
├── src/main/java/com/editor/
│   ├── Main.java                      # 程序入口
│   ├── core/                          # 核心模块
│   │   ├── Workspace.java             # 工作区管理
│   │   ├── Editor.java                # 编辑器抽象基类
│   │   └── TextEditor.java            # 文本编辑器实现
│   ├── command/                       # 命令模块
│   │   ├── Command.java               # 命令接口
│   │   ├── CommandHistory.java        # 命令历史
│   │   ├── workspace/                 # 工作区命令
│   │   └── text/                      # 文本编辑命令
│   ├── pattern/                       # 设计模式实现
│   │   ├── memento/                   # 备忘录模式
│   │   └── observer/                  # 观察者模式
│   ├── logging/                       # 日志模块
│   ├── cli/                           # 命令行接口
│   ├── exception/                     # 自定义异常
│   └── util/                          # 工具类
└── src/test/java/                     # 测试代码
```

## 构建和运行

### 前置要求

- Java 11+
- Maven 3.6+

### 编译项目

```bash
mvn clean compile
```

### 运行程序

```bash
mvn exec:java -Dexec.mainClass="com.editor.Main"
```

### 打包

```bash
mvn clean package
java -jar target/text-editor-1.0.0.jar
```

### 运行测试

```bash
mvn test
```

## 使用说明

详细的使用示例请参考 [USAGE_EXAMPLE.md](USAGE_EXAMPLE.md)

### 启动编辑器

启动后会进入交互式命令行界面：

```
文本编辑器 v1.0
输入 'help' 或 'h' 查看帮助
输入 'quit' 或 'exit' 退出

> 
```

### 可用命令

**文件操作:**
- `load <filepath>` 或 `l <filepath>` - 加载文件
- `create <filepath>` 或 `new/n <filepath>` - 创建新文件
- `save` 或 `s` - 保存当前文件
- `close <filepath>` 或 `c <filepath>` - 关闭文件
- `init` - 初始化工作区
- `edit <filepath>` 或 `e <filepath>` - 切换到指定文件
- `list` - 列出所有打开的文件

**文本编辑:**
- `show` - 显示当前文件内容
- `append <content>` 或 `a <content>` - 追加一行
- `insert <line> <content>` 或 `i <line> <content>` - 插入一行
- `delete <line>` 或 `d <line>` - 删除一行
- `replace <line> <content>` 或 `r <line> <content>` - 替换一行

**其他:**
- `undo` - 撤销
- `redo` - 重做
- `tree [dir]` - 显示目录树
- `help` 或 `h` - 显示帮助
- `quit` 或 `exit` 或 `q` - 退出

### 示例

```bash
> create test.txt
已创建并打开文件: test.txt

> show
(文件为空)

> append Hello World
已追加

> show
Hello World

> append This is a new line
已追加

> show
Hello World
This is a new line

> undo
已撤销

> show
Hello World

> save
文件已保存

> quit
再见！
```

## 技术栈

- **语言**: Java 11
- **构建工具**: Maven
- **测试框架**: JUnit 5
- **JSON序列化**: Gson
- **文件操作**: Commons IO


