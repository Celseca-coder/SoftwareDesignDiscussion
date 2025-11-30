package com.editor.core.logging;

/**
 * 事件监听器接口（观察者模式）
 */
public interface EventListener {
    /**
     * 处理事件
     * @param event 编辑器事件
     */
    void onEvent(EditorEvent event);
}
