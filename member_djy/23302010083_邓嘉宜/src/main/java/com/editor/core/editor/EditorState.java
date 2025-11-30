package com.editor.core.editor;

import java.util.ArrayList;
import java.util.List;

/**
 * 编辑器状态（用于Memento模式和undo/redo）
 */
public class EditorState {
    private List<String> lines;
    
    public EditorState(List<String> lines) {
        // 深拷贝，如果传入null则创建空列表
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }
    
    public List<String> getLines() {
        // 返回副本，避免外部修改
        return new ArrayList<>(lines);
    }
}
