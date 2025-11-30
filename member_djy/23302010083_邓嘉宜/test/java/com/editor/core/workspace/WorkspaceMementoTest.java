package com.editor.core.workspace;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WorkspaceMemento 单元测试
 * 测试工作区备忘录类（Memento模式）
 */
public class WorkspaceMementoTest {
    
    /**
     * 测试正常构造 WorkspaceMemento 的场景。
     * 测试数据：openFiles=["file1.txt","file2.txt"]，activeFile="file1.txt"，modifiedStatus 中 file1=true、file2=false，loggingEnabled 中 file1=true。
     * 预期：备忘录中各字段与传入数据一致，列表和映射内容正确。
     */
    @Test
    void testWorkspaceMementoCreation() {
        List<String> openFiles = Arrays.asList("file1.txt", "file2.txt");
        String activeFile = "file1.txt";
        Map<String, Boolean> modifiedStatus = new HashMap<>();
        modifiedStatus.put("file1.txt", true);
        modifiedStatus.put("file2.txt", false);
        Map<String, Boolean> loggingEnabled = new HashMap<>();
        loggingEnabled.put("file1.txt", true);
        
        WorkspaceMemento memento = new WorkspaceMemento(
            openFiles, activeFile, modifiedStatus, loggingEnabled
        );
        
        assertNotNull(memento);
        List<String> retrievedFiles = memento.getOpenFiles();
        assertEquals(2, retrievedFiles.size());
        assertEquals("file1.txt", memento.getActiveFile());
        
        Map<String, Boolean> retrievedModified = memento.getModifiedStatus();
        assertTrue(retrievedModified.get("file1.txt"));
        assertFalse(retrievedModified.get("file2.txt"));
        
        Map<String, Boolean> retrievedLogging = memento.getLoggingEnabled();
        assertTrue(retrievedLogging.get("file1.txt"));
    }
    
    /**
     * 测试 WorkspaceMemento 对传入集合的防御性拷贝（不可变性）。
     * 测试数据：openFiles 初始只包含 "file1.txt"，modifiedStatus 中 "file1.txt"=true，构造后再修改原始列表和 map。
     * 预期：从备忘录取出的列表仍然只有一个元素，且 "file1.txt" 的 modifiedStatus 仍为 true。
     */
    @Test
    void testWorkspaceMementoImmutable() {
        List<String> openFiles = new java.util.ArrayList<>();
        openFiles.add("file1.txt");
        
        Map<String, Boolean> modifiedStatus = new HashMap<>();
        modifiedStatus.put("file1.txt", true);
        
        WorkspaceMemento memento = new WorkspaceMemento(
            openFiles, "file1.txt", modifiedStatus, new HashMap<>()
        );
        
        // 修改原始数据不应该影响备忘录
        openFiles.add("file2.txt");
        modifiedStatus.put("file1.txt", false);
        
        List<String> retrievedFiles = memento.getOpenFiles();
        assertEquals(1, retrievedFiles.size());
        
        Map<String, Boolean> retrievedModified = memento.getModifiedStatus();
        assertTrue(retrievedModified.get("file1.txt"));
    }
    
    /**
     * 测试 WorkspaceMemento 在传入 null 时的安全处理。
     * 测试数据：openFiles=null，activeFile=null，modifiedStatus=null，loggingEnabled=null。
     * 预期：getOpenFiles 返回空列表，getActiveFile 返回 null，两个 map 均为空。
     */
    @Test
    void testWorkspaceMementoWithNull() {
        WorkspaceMemento memento = new WorkspaceMemento(
            null, null, null, null
        );
        
        assertNotNull(memento);
        assertTrue(memento.getOpenFiles().isEmpty());
        assertNull(memento.getActiveFile());
        assertTrue(memento.getModifiedStatus().isEmpty());
        assertTrue(memento.getLoggingEnabled().isEmpty());
    }
    
    /**
     * 测试 WorkspaceMemento 返回的集合是副本而不是内部引用。
     * 测试数据：openFiles=["file1.txt"]，modifiedStatus 中 "file1.txt"=true。
     * 预期：对 getOpenFiles 和 getModifiedStatus 的多次调用返回不同实例，对返回集合的修改不会影响下次读取的结果。
     */
    @Test
    void testWorkspaceMementoReturnsCopies() {
        List<String> openFiles = Arrays.asList("file1.txt");
        Map<String, Boolean> modifiedStatus = new HashMap<>();
        modifiedStatus.put("file1.txt", true);
        
        WorkspaceMemento memento = new WorkspaceMemento(
            openFiles, "file1.txt", modifiedStatus, new HashMap<>()
        );
        
        List<String> retrieved1 = memento.getOpenFiles();
        List<String> retrieved2 = memento.getOpenFiles();
        
        // 应该返回不同的副本
        assertNotSame(retrieved1, retrieved2);
        
        Map<String, Boolean> retrievedMap1 = memento.getModifiedStatus();
        Map<String, Boolean> retrievedMap2 = memento.getModifiedStatus();
        
        assertNotSame(retrievedMap1, retrievedMap2);
    }
    
    /**
     * 测试空工作区的 WorkspaceMemento。
     * 测试数据：openFiles 为空列表，activeFile=null，两个 map 为空。
     * 预期：各字段均为空或 empty，不出现异常。
     */
    @Test
    void testWorkspaceMementoEmpty() {
        WorkspaceMemento memento = new WorkspaceMemento(
            Arrays.asList(), null, new HashMap<>(), new HashMap<>()
        );
        
        assertTrue(memento.getOpenFiles().isEmpty());
        assertNull(memento.getActiveFile());
        assertTrue(memento.getModifiedStatus().isEmpty());
        assertTrue(memento.getLoggingEnabled().isEmpty());
    }
}
