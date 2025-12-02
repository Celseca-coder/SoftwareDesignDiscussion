package com.editor.core.editor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

/**
 * EditorState 单元测试
 * 测试编辑器状态类
 */
public class EditorStateTest {
    
    /**
     * 测试 EditorState 的创建。
     * 测试数据：包含三行的列表 ["Line 1", "Line 2", "Line 3"]。
     * 预期：状态对象不为空，返回的行列表大小为3，第一行内容为 "Line 1"，以此类推。
     */
    @Test
    void testEditorStateCreation() {
        List<String> lines = Arrays.asList("Line 1", "Line 2", "Line 3");
        EditorState state = new EditorState(lines);
        
        assertNotNull(state);
        List<String> retrievedLines = state.getLines();
        assertEquals(3, retrievedLines.size());
        assertEquals("Line 1", retrievedLines.get(0));
        assertEquals("Line 2", retrievedLines.get(1));
        assertEquals("Line 3", retrievedLines.get(2));
    }
    
    /**
     * 测试 EditorState 的不可变性。
     * 测试数据：初始列表 ["Line 1", "Line 2"]，创建状态后修改原列表添加 "Line 3"。
     * 预期：状态内部的行列表不受原列表修改影响，仍为2行。
     */
    @Test
    void testEditorStateImmutable() {
        List<String> originalLines = new java.util.ArrayList<>();
        originalLines.add("Line 1");
        originalLines.add("Line 2");
        
        EditorState state = new EditorState(originalLines);
        
        // 修改原始列表不应该影响状态
        originalLines.add("Line 3");
        List<String> stateLines = state.getLines();
        assertEquals(2, stateLines.size());
    }
    
    /**
     * 测试 getLines() 返回副本。
     * 测试数据：列表 ["Line 1", "Line 2"]。
     * 预期：每次调用 getLines() 返回新对象，修改返回列表不影响状态内部数据。
     */
    @Test
    void testEditorStateGetLinesReturnsCopy() {
        List<String> lines = Arrays.asList("Line 1", "Line 2");
        EditorState state = new EditorState(lines);
        
        List<String> retrievedLines1 = state.getLines();
        List<String> retrievedLines2 = state.getLines();
        
        // 每次调用应该返回新的副本
        assertNotSame(retrievedLines1, retrievedLines2);
        
        // 修改返回的列表不应该影响状态
        retrievedLines1.add("Line 3");
        List<String> retrievedLines3 = state.getLines();
        assertEquals(2, retrievedLines3.size());
    }
    
    /**
     * 测试空列表的 EditorState。
     * 测试数据：空列表 []。
     * 预期：返回的行列表为空。
     */
    @Test
    void testEditorStateWithEmptyList() {
        List<String> emptyLines = Arrays.asList();
        EditorState state = new EditorState(emptyLines);
        
        List<String> retrievedLines = state.getLines();
        assertTrue(retrievedLines.isEmpty());
    }
    
    /**
     * 测试 null 输入的 EditorState。
     * 测试数据：null。
     * 预期：返回的行列表不为空（假设内部处理为新列表）。
     */
    @Test
    void testEditorStateWithNull() {
        // 测试null处理（如果允许）
        EditorState state = new EditorState(null);
        List<String> retrievedLines = state.getLines();
        // 应该返回空列表或处理null
        assertNotNull(retrievedLines);
    }
}
