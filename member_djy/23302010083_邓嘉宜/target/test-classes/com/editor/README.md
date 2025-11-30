# 测试代码

本目录用于存放单元测试和集成测试代码。

## 测试结构

已实现的测试类：

### 编辑器模块测试
- `core/editor/TextEditorTest.java` - TextEditor单元测试
  - 测试append, insert, delete, replace, show操作
  - 测试undo/redo功能
  - 测试边界情况和异常处理
- `core/editor/EditorStateTest.java` - EditorState单元测试
  - 测试状态创建和不可变性

### 工作区模块测试
- `core/workspace/WorkspaceTest.java` - Workspace单元测试
  - 测试文件打开/关闭
  - 测试活动文件管理
  - 测试修改状态跟踪
  - 测试Memento模式
  - 测试事件监听
- `core/workspace/WorkspaceMementoTest.java` - WorkspaceMemento单元测试
  - 测试备忘录创建和恢复
- `core/workspace/EditorManagerTest.java` - EditorManager单元测试
  - 测试编辑器管理器功能

### 命令模块测试
- `core/command/CommandManagerTest.java` - CommandManager单元测试
  - 测试命令执行
  - 测试undo/redo功能

### CLI模块测试
- `ui/cli/CommandParserTest.java` - CommandParser单元测试
  - 测试命令解析
  - 测试参数解析（位置、范围、整数）
  - 测试引号处理

### 日志模块测试
- `core/logging/LoggingServiceTest.java` - LoggingService单元测试
  - 测试日志开关
  - 测试事件处理
  - 测试日志读取

## 测试框架

使用JUnit 5进行单元测试。

## 运行测试

### 使用Maven运行测试
```bash
mvn test
```

### 使用Gradle运行测试
```bash
gradle test
```

### 使用JUnit Platform Console Launcher
```bash
# 编译测试代码
javac -encoding UTF-8 -d test-classes -cp "target/classes:junit-platform-console-standalone.jar" test/java/**/*.java

# 运行测试
java -jar junit-platform-console-standalone.jar --class-path target/classes:test-classes --scan-class-path
```

### 使用IDE运行
在IntelliJ IDEA或Eclipse中：
1. 右键点击测试类
2. 选择"Run Test"或"Debug Test"

## 测试覆盖率

当前测试覆盖：
- 编辑器模块：TextEditor所有基本操作
- 工作区模块：Workspace文件管理和状态管理
- 命令模块：CommandManager undo/redo
- CLI模块：CommandParser解析功能
- 日志模块：LoggingService基本功能
- Memento模式：状态保存和恢复

## 注意事项

- 测试代码应与源代码分离
- 每个模块都有对应的测试类
- 测试覆盖主要功能和边界情况
- 使用JUnit 5的注解和断言
