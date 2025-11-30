# 文本编辑器系统

基于命令行的文本编辑器，支持多文件编辑、工作区管理、日志记录和状态持久化。

## 项目结构

```
23302010083_邓嘉宜/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── editor/
│                   ├── Main.java
│                   ├── core/
│                   │   ├── workspace/       # 工作区模块
│                   │   ├── editor/          # 编辑器模块
│                   │   ├── command/         # 命令模块
│                   │   ├── logging/         # 日志模块
│                   │   └── persistence/     # 持久化模块
│                   └── ui/
│                       └── cli/             # 命令行界面
├── test/
│   └── java/                                # 测试代码
├── docs/
│   └── 架构设计.pdf                          # 架构设计文档
│   └── 测试命令.pdf                          # 测试命令文档
└── README.md
└── pom.xml
```

## 编译和运行

### 使用命令行编译运行

```bash
# 编译（Windows PowerShell）
javac -encoding UTF-8 -d out -sourcepath src/main/java src/main/java/com/editor/**/*.java

# 运行
java -cp out com.editor.Main
```

### 使用Maven（推荐）

```bash
# 编译
mvn compile

# 运行
mvn exec:java -Dexec.mainClass="com.editor.Main"
```

## 功能特性

### 工作区命令（10个）

- `load <file>` - 加载文件到编辑器
- `save [file]` - 保存文件
- `init <file>` - 初始化新文件
- `close [file]` - 关闭文件
- `edit <file>` - 切换到指定文件
- `editor-list` - 列出所有打开的文件
- `dir-tree [path]` - 显示目录树
- `undo` - 撤销上一次操作
- `redo` - 重做上一次撤销的操作
- `exit` - 退出程序

### 文本编辑命令（5个）

- `append "text" [file]` - 在文件末尾追加文本
- `insert <line:col> "text" [file]` - 在指定位置插入文本
- `delete <line:col> <len> [file]` - 删除指定位置的字符
- `replace <line:col> <len> "text" [file]` - 替换指定位置的字符
- `show [startLine:endLine] [file]` - 显示文本内容

### 日志命令（3个）

- `log-on [file]` - 启用日志记录
- `log-off [file]` - 关闭日志记录
- `log-show [file]` - 显示日志记录

## 使用示例

```
> load test.txt
> append "Hello World"
> show
1: Hello World
> insert 1:6 ", "
> show
1: Hello,  World
> save
> exit
```

## 设计模式

- **命令模式 (Command Pattern)**: 实现命令系统和undo/redo功能
- **备忘录模式 (Memento Pattern)**: 实现工作区状态持久化
- **观察者模式 (Observer Pattern)**: 实现日志事件通知机制
- **装饰器模式 (Decorator Pattern)**: 为未来扩展预留（当前未实现）

## 编码规范

- 文件编码：UTF-8
- Java版本：Java 8 或更高
- 代码风格：遵循Java编码规范

## 依赖管理

本实验仅使用Java标准库，不使用第三方库。


