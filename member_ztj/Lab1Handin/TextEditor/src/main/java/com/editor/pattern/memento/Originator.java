package com.editor.pattern.memento;

/**
 * 发起人类
 * 创建备忘录并可以从备忘录恢复状态
 */
public interface Originator<T extends Memento> {
    /**
     * 创建备忘录
     */
    T createMemento();
    
    /**
     * 从备忘录恢复状态
     */
    void restoreFromMemento(T memento);
}

