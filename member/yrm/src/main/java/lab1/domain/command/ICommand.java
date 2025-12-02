package lab1.domain.command;


public interface ICommand {
    void execute();
    void undo();
    String getDescription(); // 用于日志记录
}
