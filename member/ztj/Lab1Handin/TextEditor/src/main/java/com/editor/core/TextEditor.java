package com.editor.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 文本编辑器实现
 * 管理文本内容的加载、保存和基本操作
 */
public class TextEditor extends Editor {
    private List<String> lines;
    
    public TextEditor(String filepath) {
        super(filepath);
        this.lines = new ArrayList<>();
    }
    
    @Override
    public void load() throws Exception {
        try {
            if (Files.exists(Paths.get(filepath))) {
                lines = Files.readAllLines(Paths.get(filepath));
            } else {
                lines = new ArrayList<>();
            }
        } catch (IOException e) {
            throw new Exception("加载文件失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void save() throws Exception {
        try {
            Files.write(Paths.get(filepath), lines);
            markSaved();
        } catch (IOException e) {
            throw new Exception("保存文件失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getContent() {
        return String.join("\n", lines);
    }
    
    /**
     * 获取行列表
     */
    public List<String> getLines() {
        return new ArrayList<>(lines);
    }
    
    /**
     * 设置行列表
     */
    public void setLines(List<String> lines) {
        this.lines = new ArrayList<>(lines);
        markModified();
    }
    
    /**
     * 获取指定行
     */
    public String getLine(int lineNumber) {
        if (lineNumber < 0 || lineNumber >= lines.size()) {
            return null;
        }
        return lines.get(lineNumber);
    }
    
    /**
     * 设置指定行
     */
    public void setLine(int lineNumber, String content) {
        if (lineNumber >= 0 && lineNumber < lines.size()) {
            lines.set(lineNumber, content);
            markModified();
        }
    }
    
    /**
     * 在指定位置插入行
     */
    public void insertLine(int lineNumber, String content) {
        if (lineNumber >= 0 && lineNumber <= lines.size()) {
            lines.add(lineNumber, content);
            markModified();
        }
    }
    
    /**
     * 删除指定行
     */
    public void deleteLine(int lineNumber) {
        if (lineNumber >= 0 && lineNumber < lines.size()) {
            lines.remove(lineNumber);
            markModified();
        }
    }
    
    /**
     * 追加行
     */
    public void appendLine(String content) {
        lines.add(content);
        markModified();
    }
    
    /**
     * 获取行数
     */
    public int getLineCount() {
        return lines.size();
    }
}

