package lab1.infrastructure;

import lab1.application.WorkspaceState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 目标：集成测试 ConfigManager 和 FileSystem
 * 验证：WorkspaceState 是否能被正确地序列化到磁盘，并被反序列化回来
 * TDD 原则：测试无副作用，每次运行后清理 .editor_workspace 文件 [cite: 335]
 */
class ConfigManagerTest {

    private final String CONFIG_FILE = ".editor_workspace";
    private IFileSystem fileSystem;
    private ConfigManager configManager;

    @BeforeEach
    void setUp() {
        // 我们使用 *真实* 的 FileSystem 进行集成测试
        this.fileSystem = new FileSystem();

        // 注入真实的 FileSystem
        this.configManager = ConfigManager.getTestInstance(this.fileSystem);

        // 确保测试前没有旧的配置文件
        cleanup();
    }

    @AfterEach
    void cleanup() {
        File f = new File(CONFIG_FILE);
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    void testSaveAndLoadRoundtrip() {
        // 准备 (Arrange)
        // 创建一个模拟的状态对象
        WorkspaceState originalState = new WorkspaceState(
                List.of("file1.txt", "file2.txt"),
                "file1.txt",
                Set.of("file2.txt"),
                Set.of("file1.txt")
        );

        // 执行 (Act) - 保存
        configManager.save(originalState);

        // 断言 (Assert) - 文件是否真实存在
        assertTrue(fileSystem.fileExists(CONFIG_FILE), "ConfigManager.save() 没有创建 .editor_workspace 文件");

        // 执行 (Act) - 加载
        WorkspaceState loadedState = configManager.load();

        // 断言 (Assert) - 加载回来的数据是否一致
        assertNotNull(loadedState);
        assertEquals("file1.txt", loadedState.getActiveFile());
        assertEquals(2, loadedState.getOpenFiles().size());
        assertTrue(loadedState.getModifiedFiles().contains("file2.txt"));
        assertTrue(loadedState.getLogEnabledFiles().contains("file1.txt"));
    }
}