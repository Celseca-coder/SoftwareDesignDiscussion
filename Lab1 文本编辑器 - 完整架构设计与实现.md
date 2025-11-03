# Lab1 文本编辑器 - 完整架构设计与实现

## 第一部分：最终架构设计

### 1. 整体架构概览

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌──────────────┐  ┌─────────────┐  ┌──────────────┐       │
│  │ CommandParser│  │CommandExecutor│ │  ConsoleUI   │       │
│  └──────────────┘  └─────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                         │
│  ┌──────────────┐  ┌─────────────┐  ┌──────────────┐       │
│  │  Workspace   │←→│  EventBus   │←→│LoggingService│       │
│  │  (Singleton) │  │ (Singleton) │  │  (Observer)  │       │
│  └──────────────┘  └─────────────┘  └──────────────┘       │
│         ↓                                                    │
│  ┌──────────────┐  ┌─────────────┐                         │
│  │WorkspaceState│  │StateManager │                         │
│  │  (Memento)   │  │  (Caretaker)│                         │
│  └──────────────┘  └─────────────┘                         │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                            │
│  ┌──────────────┐  ┌─────────────┐  ┌──────────────┐       │
│  │   IEditor    │  │EditorFactory│  │CommandHistory│       │
│  │  (Strategy)  │  │  (Factory)  │  │              │       │
│  └──────────────┘  └─────────────┘  └──────────────┘       │
│         ↑                                    ↓               │
│  ┌──────────────┐                    ┌──────────────┐       │
│  │  TextEditor  │                    │   ICommand   │       │
│  │              │                    │  (Command)   │       │
│  └──────────────┘                    └──────────────┘       │
│                                             ↑                │
│                      ┌──────────────────────┴────────┐      │
│               ┌──────────┐  ┌──────────┐  ┌─────────┐      │
│               │AppendCmd │  │InsertCmd │  │DeleteCmd│ ...  │
│               └──────────┘  └──────────┘  └─────────┘      │
│                                                              │
│  ┌─────────────────────────────────────────────────┐       │
│  │        FileSystem Tree (Composite)              │       │
│  │  ┌──────────────┐  ┌─────────────┐             │       │
│  │  │FileSystemNode│  │TreeVisitor  │             │       │
│  │  │  (Component) │  │  (Visitor)  │             │       │
│  │  └──────────────┘  └─────────────┘             │       │
│  │         ↑                  ↑                    │       │
│  │    ┌────┴────┐      ┌──────────┐               │       │
│  │ ┌────┐  ┌─────┐  ┌────────────┐               │       │
│  │ │File│  │Dir  │  │TreeDisplay │               │       │
│  │ │Node│  │Node │  │  Visitor   │               │       │
│  │ └────┘  └─────┘  └────────────┘               │       │
│  └─────────────────────────────────────────────────┘       │
│                                                              │
│  ┌─────────────────────────────────────────────────┐       │
│  │      Content Display (Decorator)                │       │
│  │  ┌──────────────┐  ┌─────────────┐             │       │
│  │  │IContentDisplayer│→│LineNumber  │             │       │
│  │  │               │  │ Decorator  │             │       │
│  │  └──────────────┘  └─────────────┘             │       │
│  └─────────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  Infrastructure Layer                        │
│  ┌──────────────┐  ┌─────────────┐  ┌──────────────┐       │
│  │  FileSystem  │  │ConfigManager│  │    Logger    │       │
│  └──────────────┘  └─────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

### 2. 各层职责详解

#### 2.1 表示层 (Presentation Layer)

**CommandParser**

- 职责：解析用户输入的命令字符串
- 处理：引号内的参数、可选参数、命令验证
- 输出：ParsedCommand对象（包含命令类型和参数）

**CommandExecutor**

- 职责：作为表示层和应用层的桥梁
- 调用：Workspace的相应方法执行命令
- 返回：ExecutionResult（成功/失败及消息）

**ConsoleUI**

- 职责：主循环、用户交互、结果展示
- 处理：确认对话框（如close时的保存提示）

#### 2.2 应用层 (Application Layer)

**Workspace (单例模式)**

- 核心职责：

  - 管理所有打开的编辑器（Map<String, IEditor>）
  - 维护当前活动编辑器引用
  - 协调命令执行流程
  - 管理文件的修改状态和日志开关状态

- 关键方法：

  ```java
  - loadFile(path): 加载/创建文件
  - saveFile(path): 保存文件
  - closeFile(path): 关闭文件（带保存提示）
  - executeEditCommand(ICommand): 执行可撤销命令
  - undo(): 撤销操作
  - redo(): 重做操作
  - createMemento(): 创建状态快照
  - restoreFromMemento(memento): 恢复状态
  ```

- 事件发布：

  ```java
  - 执行命令后 → CommandExecutedEvent
  - 保存文件后 → FileSavedEvent
  - 加载文件后 → FileLoadedEvent
  ```

**WorkspaceState (备忘录模式)**

- 保存数据：

  ```java
  - openFiles: List<String>              // 文件路径列表- activeFile: String                   // 当前活动文件- modifiedFiles: Set<String>           // 已修改文件集合- logEnabledFiles: Set<String>         // 启用日志的文件
  ```

**StateManager (备忘录管理者)**

- 职责：序列化和反序列化工作区状态
- 格式：JSON格式存储到`.editor_workspace`
- 时机：程序启动时加载，exit时保存

**EventBus (单例模式 + 观察者模式核心)**

- 职责：事件分发中心

- 实现：

  ```java
  - Map<Class<? extends Event>, List<EventListener>>- publish(Event): 发布事件- subscribe(Class<Event>, EventListener): 订阅事件
  ```

**LoggingService (观察者模式)**

- 职责：监听事件并记录日志
- 订阅：CommandExecutedEvent
- 行为：
  - 检查该文件是否启用日志
  - 写入`.filename.log`文件
  - 格式：`timestamp command parameters`
  - 失败时仅警告，不中断程序

#### 2.3 领域层 (Domain Layer)

**IEditor 接口 (策略模式)**

```java
interface IEditor {
    void executeCommand(ICommand command);
    String getContent();
    List<String> getLines();
    boolean isModified();
    void setModified(boolean modified);
    void undo();
    void redo();
    boolean canUndo();
    boolean canRedo();
}
```

**TextEditor (策略模式实现)**

- 数据结构：`List<String> lines` - 按行存储
- 命令历史：持有CommandHistory对象
- 实现：所有IEditor接口方法

**EditorFactory (工厂模式)**

```java
class EditorFactory {
    public static IEditor createEditor(String filePath) {
        if (filePath.endsWith(".txt")) {
            return new TextEditor(filePath);
        }
        // 为Lab2预留扩展点
        // if (filePath.endsWith(".xml")) {
        //     return new XMLEditor(filePath);
        // }
        throw new UnsupportedOperationException("不支持的文件类型");
    }
}
```

**ICommand 接口 (命令模式)**

```java
interface ICommand {
    void execute();
    void undo();
    String getDescription();  // 用于日志记录
}
```

**具体命令类**

1. **AppendCommand**
   - 执行：在末尾添加一行
   - 撤销：删除最后一行
   - 记录：追加的文本内容
2. **InsertCommand**
   - 执行：在指定位置插入文本（可跨行）
   - 撤销：删除插入的内容
   - 记录：插入位置、内容、影响的行数
3. **DeleteCommand**
   - 执行：删除指定长度的字符
   - 撤销：恢复删除的内容到原位置
   - 记录：删除位置、删除的内容
4. **ReplaceCommand**
   - 执行：删除指定长度+插入新文本
   - 撤销：恢复原文本
   - 记录：位置、原文本、新文本

**CommandHistory**

```java
class CommandHistory {
    private Stack<ICommand> undoStack;
    private Stack<ICommand> redoStack;
    
    void push(ICommand cmd);
    ICommand popUndo();
    ICommand popRedo();
    void clearRedo();  // 执行新命令时清空
}
```

**FileSystemNode (组合模式)**

```java
interface FileSystemNode {
    String getName();
    boolean isDirectory();
    void accept(TreeVisitor visitor);
}

class FileNode implements FileSystemNode {
    private String name;
    // 叶节点：代表文件
}

class DirectoryNode implements FileSystemNode {
    private String name;
    private List<FileSystemNode> children;
    
    public void addChild(FileSystemNode node);
    public List<FileSystemNode> getChildren();
}
```

**TreeVisitor (访问者模式)**

```java
interface TreeVisitor {
    void visitFile(FileNode file, String prefix, boolean isLast);
    void visitDirectory(DirectoryNode dir, String prefix, boolean isLast);
}

class TreeDisplayVisitor implements TreeVisitor {
    private StringBuilder output;
    
    @Override
    public void visitFile(FileNode file, String prefix, boolean isLast) {
        output.append(prefix);
        output.append(isLast ? "└── " : "├── ");
        output.append(file.getName());
        output.append("\n");
    }
    
    @Override
    public void visitDirectory(DirectoryNode dir, String prefix, boolean isLast) {
        output.append(prefix);
        output.append(isLast ? "└── " : "├── ");
        output.append(dir.getName());
        output.append("\n");
        
        List<FileSystemNode> children = dir.getChildren();
        String childPrefix = prefix + (isLast ? "    " : "│   ");
        for (int i = 0; i < children.size(); i++) {
            children.get(i).accept(this, childPrefix, i == children.size() - 1);
        }
    }
    
    public String getOutput() {
        return output.toString();
    }
}
```

**IContentDisplayer (装饰器模式)**

```java
interface IContentDisplayer {
    String display(List<String> lines, int startLine, int endLine);
}

class BasicContentDisplayer implements IContentDisplayer {
    @Override
    public String display(List<String> lines, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start - 1; i < end && i < lines.size(); i++) {
            sb.append(lines.get(i)).append("\n");
        }
        return sb.toString();
    }
}

class LineNumberDecorator implements IContentDisplayer {
    private IContentDisplayer wrapped;
    
    public LineNumberDecorator(IContentDisplayer wrapped) {
        this.wrapped = wrapped;
    }
    
    @Override
    public String display(List<String> lines, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start - 1; i < end && i < lines.size(); i++) {
            sb.append((i + 1)).append(": ");
            sb.append(lines.get(i)).append("\n");
        }
        return sb.toString();
    }
}
```

#### 2.4 基础设施层 (Infrastructure Layer)

**FileSystem**

```java
class FileSystem {
    public static String readFile(String path);
    public static void writeFile(String path, String content);
    public static boolean fileExists(String path);
    public static FileSystemNode buildTree(String path);
}
```

**ConfigManager**

- 管理`.editor_workspace`文件的读写
- JSON序列化/反序列化

**Logger**

- 写入`.filename.log`文件
- 格式化时间戳
- 处理IO异常

### 3. 关键交互流程

#### 3.1 执行编辑命令流程

```
用户输入 "insert 1:1 Hello"
    ↓
CommandParser.parse() → ParsedCommand{type=INSERT, args=[1:1, Hello]}
    ↓
CommandExecutor.execute(parsedCmd)
    ↓
Workspace.executeEditCommand()
    ├→ 创建 InsertCommand(textEditor, 1, 1, "Hello")
    ├→ command.execute()
    │   ├→ textEditor.lines 修改
    │   └→ textEditor.setModified(true)
    ├→ commandHistory.push(command)
    └→ EventBus.publish(new CommandExecutedEvent("insert 1:1 Hello"))
        ↓
    LoggingService.onEvent()
        ├→ 检查文件是否启用日志
        ├→ 格式化：timestamp + command
        └→ Logger.writeLog(".lab.txt.log", entry)
```

#### 3.2 Undo/Redo流程

```
Undo:
    Workspace.undo()
        ↓
    textEditor.undo()
        ↓
    CommandHistory.popUndo() → command
        ↓
    command.undo()
        ↓
    CommandHistory.pushRedo(command)

Redo:
    类似，但从redoStack弹出并压入undoStack
```

#### 3.3 Dir-Tree流程（组合+访问者）

```
用户输入 "dir-tree"
    ↓
FileSystem.buildTree(currentPath)
    ├→ 递归构建 DirectoryNode 和 FileNode 树
    └→ 返回根节点 rootNode
        ↓
TreeDisplayVisitor visitor = new TreeDisplayVisitor()
rootNode.accept(visitor, "", true)
    ├→ visitDirectory() 被调用
    ├→ 递归访问所有子节点
    │   ├→ visitFile() 打印文件
    │   └→ visitDirectory() 打印目录
    └→ 构建输出字符串
        ↓
ConsoleUI.print(visitor.getOutput())
```

#### 3.4 Show命令流程（装饰器）

```
用户输入 "show 1:5"
    ↓
Workspace.showContent(1, 5)
    ↓
IContentDisplayer displayer = new LineNumberDecorator(
                                new BasicContentDisplayer());
    ↓
displayer.display(textEditor.getLines(), 1, 5)
    ↓
输出：
1: 第一行内容
2: 第二行内容
...
```

#### 3.5 程序退出流程（备忘录）

```
用户输入 "exit"
    ↓
检查所有打开的文件是否有未保存修改
    ├→ 有修改：提示 "文件 xxx 已修改，是否保存? (y/n)"
    └→ 用户确认
        ↓
WorkspaceState memento = workspace.createMemento()
    memento.openFiles = [...]
    memento.activeFile = "..."
    memento.modifiedFiles = {...}
    memento.logEnabledFiles = {...}
        ↓
StateManager.save(memento)
    ├→ 序列化为JSON
    └→ 写入 .editor_workspace
        ↓
System.exit(0)
```

### 4. 设计模式协同总结

| 设计模式       | 位置              | 作用                        | 协同关系                                   |
| -------------- | ----------------- | --------------------------- | ------------------------------------------ |
| **命令模式**   | Domain Layer      | 封装编辑操作，实现undo/redo | 与观察者模式协同：命令执行后发布事件       |
| **观察者模式** | Application Layer | EventBus解耦事件发布和订阅  | 日志模块订阅命令执行事件                   |
| **备忘录模式** | Application Layer | 保存和恢复Workspace状态     | 独立于其他模式，在exit时使用               |
| **策略模式**   | Domain Layer      | IEditor接口定义编辑器抽象   | 与工厂模式协同：工厂根据文件类型创建策略   |
| **工厂模式**   | Domain Layer      | 根据文件类型创建编辑器      | 与策略模式协同：创建不同的策略实现         |
| **单例模式**   | Application Layer | Workspace和EventBus全局唯一 | 与观察者模式协同：EventBus作为单例事件中心 |
| **组合模式**   | Domain Layer      | 统一表示文件和目录树        | 与访问者模式协同：组合结构接受访问者遍历   |
| **访问者模式** | Domain Layer      | 封装树形结构的打印逻辑      | 与组合模式协同：访问组合结构的节点         |
| **装饰器模式** | Domain Layer      | 动态添加行号显示功能        | 独立使用，为show命令提供灵活的显示方式     |

------

