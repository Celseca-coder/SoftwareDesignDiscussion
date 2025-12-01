package lab1.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

// 目标：测试 FileSystem 是否能 *真正* 与磁盘交互 (集成测试)
// TDD.pdf 原则：测试无副作用，运行后状态不变
class FileSystemTest {

    private final IFileSystem fileSystem = new FileSystem();
    private final String TEST_FILE = "temp_test_file.txt";
    private final String TEST_CONTENT = "Hello Infrastructure Test";

    @BeforeEach
    @AfterEach
    void cleanup() {
        // 确保每个测试前后，文件都不存在
        File f = new File(TEST_FILE);
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    void testWriteReadFile() throws IOException {
        // 1. 测试文件不存在
        assertFalse(fileSystem.fileExists(TEST_FILE));

        // 2. 写入文件
        fileSystem.writeFile(TEST_FILE, TEST_CONTENT);

        // 3. 测试文件存在
        assertTrue(fileSystem.fileExists(TEST_FILE));

        // 4. 读取文件
        String content = fileSystem.readFile(TEST_FILE);

        // 5. 验证内容
        assertEquals(TEST_CONTENT, content);
    }
}