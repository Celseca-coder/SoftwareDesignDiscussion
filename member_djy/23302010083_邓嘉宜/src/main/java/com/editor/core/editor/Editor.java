package com.editor.core.editor;

import java.util.List;

/**
 * 编辑器接口
 * 定义了文本编辑器的基本操作
 */
public interface Editor {
    /**
     * 在文件末尾追加一行文本
     * @param text 要追加的文本
     */
    void append(String text);
    
    /**
     * 在指定位置插入文本
     * @param line 行号（从1开始）
     * @param col 列号（从1开始）
     * @param text 要插入的文本
     * @throws EditorException 如果位置越界
     */
    void insert(int line, int col, String text) throws EditorException;
    
    /**
     * 从指定位置删除指定长度的字符
     * @param line 行号（从1开始）
     * @param col 列号（从1开始）
     * @param len 要删除的字符长度
     * @throws EditorException 如果位置越界或长度超出
     */
    void delete(int line, int col, int len) throws EditorException;
    
    /**
     * 替换指定位置的字符
     * @param line 行号（从1开始）
     * @param col 列号（从1开始）
     * @param len 要替换的字符长度
     * @param text 替换的文本
     * @throws EditorException 如果位置越界或长度超出
     */
    void replace(int line, int col, int len, String text) throws EditorException;
    
    /**
     * 显示全文
     * @return 文本行列表
     */
    List<String> show();
    
    /**
     * 显示指定范围的内容
     * @param startLine 起始行号（从1开始）
     * @param endLine 结束行号（包含）
     * @return 文本行列表
     */
    List<String> show(int startLine, int endLine);
    
    /**
     * 检查文件是否被修改
     * @return true表示已修改
     */
    boolean isModified();
    
    /**
     * 设置文件修改状态
     * @param modified 修改状态
     */
    void setModified(boolean modified);
    
    /**
     * 获取文件路径
     * @return 文件路径
     */
    String getFilePath();
    
    /**
     * 检查是否可以撤销
     * @return true表示可以撤销
     */
    boolean canUndo();
    
    /**
     * 检查是否可以重做
     * @return true表示可以重做
     */
    boolean canRedo();
    
    /**
     * 撤销上一次操作
     */
    void undo();
    
    /**
     * 重做上一次撤销的操作
     */
    void redo();
}
