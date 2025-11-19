// EditorCommand.java
package editor.command;

/**
 * 编辑器命令接口（支持undo/redo）
 */
public interface EditorCommand extends Command {
    void undo();
}