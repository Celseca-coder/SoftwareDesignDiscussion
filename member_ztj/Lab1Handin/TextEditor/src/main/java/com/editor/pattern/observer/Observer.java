package com.editor.pattern.observer;

/**
 * 观察者接口
 * 实现观察者模式
 */
public interface Observer {
    /**
     * 接收通知
     * @param event 事件对象
     */
    void update(Event event);
}

