# 文本编辑器使用示例

## 基本使用流程

### 1. 启动编辑器

```bash
> mvn exec:java -Dexec.mainClass="com.editor.Main"
```

或者编译后运行：

```bash
> mvn clean package
> java -jar target/text-editor-1.0.0.jar
```

### 2. 创建或打开文件

#### 方式1：使用 create 命令创建新文件

```bash
> create test.txt
已创建并打开文件: test.txt

> list
打开的文件列表:
1. test.txt *
```

#### 方式2：使用 edit 命令（自动创建或打开）

```bash
# 如果文件不存在，会自动创建
> edit "L:\SoftwareDesign\Lab1Txt\words.txt"
已创建并打开文件: L:\SoftwareDesign\Lab1Txt\words.txt

# 如果文件已存在但未打开，会自动加载并打开
> edit test.txt
已加载文件: test.txt
```

#### 方式3：使用 load 命令加载现有文件

```bash
> load "L:\SoftwareDesign\Lab1Txt\words.txt"
已加载文件: L:\SoftwareDesign\Lab1Txt\words.txt
```

### 3. 编辑文本内容

```bash
# 显示当前文件内容
> show
(文件为空)

# 追加一行
> append Hello World
已追加

> show
Hello World

# 再追加一行
> append This is a text editor
已追加

> show
Hello World
This is a text editor

# 在第1行插入内容
> insert 1 Inserted line
已插入

> show
Inserted line
Hello World
This is a text editor

# 替换第3行
> replace 3 Replaced content
已替换

> show
Inserted line
Hello World
Replaced content

# 删除第2行
> delete 2
已删除

> show
Inserted line
Replaced content
```

### 4. 撤销和重做

```bash
# 撤销上一次操作
> undo
已撤销

> show
Inserted line
Hello World
Replaced content

# 再次撤销
> undo
已撤销

> show
Inserted line
Hello World
This is a text editor

# 重做操作
> redo
已重做

> show
Inserted line
Hello World
Replaced content
```

### 5. 多文件管理

```bash
# 创建第一个文件
> create file1.txt
已创建并打开文件: file1.txt

> append Content from file 1
已追加

# 创建第二个文件
> create file2.txt
已创建并打开文件: file2.txt

> append Content from file 2
已追加

# 查看所有打开的文件（*表示当前活动文件）
> list
打开的文件列表:
1. file1.txt
2. file2.txt *

# 切换到第一个文件
> edit file1.txt
已切换到文件: file1.txt

> list
打开的文件列表:
1. file1.txt *
2. file2.txt

# 显示第一个文件的内容
> show
Content from file 1
```

### 6. 保存文件

```bash
# 保存当前活动文件
> save
文件已保存
```

### 7. 关闭文件

```bash
# 关闭指定文件
> close file1.txt
已关闭文件: file1.txt

> list
打开的文件列表:
1. file2.txt *
```

### 8. 撤销创建文件

```bash
# 创建一个文件
> create temp.txt
已创建并打开文件: temp.txt

# 撤销创建操作（会删除文件并关闭）
> undo
已撤销

> list
没有打开的文件
```

### 9. 其他实用命令

```bash
# 查看帮助
> help
可用命令列表:
  load <filepath> 或 l <filepath> - 加载文件
  create <filepath> 或 new/n <filepath> - 创建新文件
  save 或 s - 保存当前文件
  close <filepath> 或 c <filepath> - 关闭文件
  init - 初始化工作区
  edit <filepath> 或 e <filepath> - 切换到指定文件
  list - 列出所有打开的文件
  tree [dir] - 显示目录树
  show - 显示当前文件内容
  append <content> 或 a <content> - 追加一行
  insert <line> <content> 或 i <line> <content> - 插入一行
  delete <line> 或 d <line> - 删除一行
  replace <line> <content> 或 r <line> <content> - 替换一行
  undo - 撤销
  redo - 重做
  help 或 h - 显示帮助
  quit 或 exit 或 q - 退出

# 显示目录树（当前目录）
> tree
.
├── src
│   └── main
│       └── java
│           └── com
│               └── editor

# 显示指定目录的目录树
> tree "L:\SoftwareDesign\Lab1Txt"
...

# 初始化工作区（关闭所有文件）
> init
工作区已初始化

> list
没有打开的文件

# 退出程序
> quit
再见！
```

## 完整示例流程

```bash
文本编辑器 v1.0
输入 'help' 或 'h' 查看帮助
输入 'quit' 或 'exit' 退出

> edit "L:\SoftwareDesign\Lab1Txt\words.txt"
已创建并打开文件: L:\SoftwareDesign\Lab1Txt\words.txt

> list
打开的文件列表:
1. L:\SoftwareDesign\Lab1Txt\words.txt *

> show
(文件为空)

> append apple
已追加

> append banana
已追加

> append cherry
已追加

> show
apple
banana
cherry

> delete 2
已删除

> show
apple
cherry

> undo
已撤销

> show
apple
banana
cherry

> save
文件已保存

> quit
再见！
```

## 注意事项

1. **路径中的空格**: 如果文件路径包含空格，请使用引号：
   ```bash
   > edit "C:\My Documents\test.txt"
   ```

2. **行号从1开始**: 所有行号参数（insert, delete, replace）都从1开始计数，不是0。

3. **自动创建目录**: 使用 `create` 或 `edit` 命令时，如果父目录不存在会自动创建。

4. **文件覆盖警告**: 如果使用 `create` 命令创建已存在的文件，会显示警告并覆盖原文件。

5. **撤销支持**: 所有文本编辑命令（append, insert, delete, replace）都支持撤销，但某些工作区命令不支持撤销。

6. **多文件编辑**: 可以同时打开多个文件，使用 `list` 查看当前打开的文件，使用 `edit <filepath>` 切换活动文件。

