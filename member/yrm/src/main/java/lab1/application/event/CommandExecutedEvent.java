package lab1.application.event;

public class CommandExecutedEvent implements Event {
    private String filePath;
    private String command;

    public CommandExecutedEvent(String filePath, String command) {
        this.filePath = filePath;
        this.command = command;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getCommand() {
        return command;
    }
}
