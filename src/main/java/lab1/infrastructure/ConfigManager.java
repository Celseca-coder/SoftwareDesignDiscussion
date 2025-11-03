package lab1.infrastructure;

import lab1.application.WorkspaceState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

public class ConfigManager {
    private static final String CONFIG_FILE = ".editor_workspace";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void save(WorkspaceState state) {
        try {
            String json = gson.toJson(state);
            FileSystem.writeFile(CONFIG_FILE, json);
        } catch (IOException e) {
            System.err.println("保存工作区状态失败: " + e.getMessage());
        }
    }

    public static WorkspaceState load() {
        try {
            if (FileSystem.fileExists(CONFIG_FILE)) {
                String json = FileSystem.readFile(CONFIG_FILE);
                return gson.fromJson(json, WorkspaceState.class);
            }
        } catch (IOException e) {
            System.err.println("加载工作区状态失败: " + e.getMessage());
        }
        return null;
    }
}