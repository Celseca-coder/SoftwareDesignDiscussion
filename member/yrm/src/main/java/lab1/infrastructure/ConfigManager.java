package lab1.infrastructure;

import lab1.application.WorkspaceState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

// 不再是一个纯静态类
public class ConfigManager {
    private static ConfigManager instance;
    private final IFileSystem fileSystem; // 依赖注入

    private static final String CONFIG_FILE = ".editor_workspace";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // 私有构造函数，用于依赖注入
    private ConfigManager(IFileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    // 公共的 getInstance()，用于生产代码 (就像 Workspace 一样)
    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager(new FileSystem());
        }
        return instance;
    }

    // (用于测试) 允许注入 Mock 的方法
    public static ConfigManager getTestInstance(IFileSystem fileSystem) {
        instance = new ConfigManager(fileSystem);
        return instance;
    }

    // 方法改为非静态
    public void save(WorkspaceState state) {
        try {
            String json = gson.toJson(state);
            // 7. 使用注入的 fileSystem 实例
            fileSystem.writeFile(CONFIG_FILE, json);
        } catch (IOException e) {
            System.err.println("保存工作区状态失败: " + e.getMessage());
        }
    }

    // 方法改为非静态
    public WorkspaceState load() {
        try {
            // 使用注入的 fileSystem 实例
            if (fileSystem.fileExists(CONFIG_FILE)) {
                String json = fileSystem.readFile(CONFIG_FILE);
                return gson.fromJson(json, WorkspaceState.class);
            }
        } catch (IOException e) {
            System.err.println("加载工作区状态失败: " + e.getMessage());
        }
        return null;
    }
}