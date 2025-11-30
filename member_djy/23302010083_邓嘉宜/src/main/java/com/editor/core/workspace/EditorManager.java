package com.editor.core.workspace;

import com.editor.core.editor.Editor;

import java.util.HashMap;
import java.util.Map;

/**
 * 编辑器管理器
 * 管理多个Editor实例
 */
public class EditorManager {
    private Map<String, Editor> editors; // 文件路径 -> Editor
    
    public EditorManager() {
        this.editors = new HashMap<>();
    }
    
    /**
     * 添加编辑器
     * @param filePath 文件路径
     * @param editor 编辑器实例
     */
    public void addEditor(String filePath, Editor editor) {
        editors.put(filePath, editor);
    }
    
    /**
     * 获取编辑器
     * @param filePath 文件路径
     * @return 编辑器实例，如果不存在返回null
     */
    public Editor getEditor(String filePath) {
        return editors.get(filePath);
    }
    
    /**
     * 移除编辑器
     * @param filePath 文件路径
     */
    public void removeEditor(String filePath) {
        editors.remove(filePath);
    }
    
    /**
     * 检查编辑器是否存在
     * @param filePath 文件路径
     * @return true表示存在
     */
    public boolean hasEditor(String filePath) {
        return editors.containsKey(filePath);
    }
    
    /**
     * 获取所有打开的文件路径
     * @return 文件路径列表
     */
    public java.util.Set<String> getAllFilePaths() {
        return editors.keySet();
    }
    
    /**
     * 获取所有编辑器
     * @return 编辑器映射
     */
    public Map<String, Editor> getAllEditors() {
        return new HashMap<>(editors);
    }
}
