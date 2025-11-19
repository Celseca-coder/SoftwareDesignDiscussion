// EventPublisher.java
package editor.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件发布器
 */
public class EventPublisher {
    private List<Observer> observers = new ArrayList<>();

    public void subscribe(Observer observer) {
        observers.add(observer);
    }

    public void unsubscribe(Observer observer) {
        observers.remove(observer);
    }

    public void publish(WorkspaceEvent event) {
        for (Observer observer : observers) {
            observer.update(event);
        }
    }
}