package com.editor.pattern.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * 主题类
 * 维护观察者列表并发送通知
 */
public abstract class Subject {
    private List<Observer> observers;
    
    public Subject() {
        this.observers = new ArrayList<>();
    }
    
    /**
     * 添加观察者
     */
    public void attach(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    /**
     * 移除观察者
     */
    public void detach(Observer observer) {
        observers.remove(observer);
    }
    
    /**
     * 通知所有观察者
     */
    public void notifyObservers(Event event) {
        for (Observer observer : observers) {
            observer.update(event);
        }
    }
    
    public List<Observer> getObservers() {
        return new ArrayList<>(observers);
    }
}

