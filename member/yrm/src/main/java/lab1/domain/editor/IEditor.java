package lab1.domain.editor;

import lab1.domain.command.ICommand;
import java.util.List;

public interface IEditor {
    void executeCommand(ICommand command);
    String getContent();
    List<String> getLines();
    boolean isModified();
    void setModified(boolean modified);
    void undo();
    void redo();
    boolean canUndo();
    boolean canRedo();
    String getFilePath();
}
