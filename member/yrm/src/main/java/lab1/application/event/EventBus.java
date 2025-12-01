package lab1.application.event;

import java.util.*;

public class EventBus {
    private static EventBus instance;
    private Map<Class<? extends Event>, List<EventListener>> listeners;

    private EventBus() {
        this.listeners = new HashMap<>();
    }

    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public <T extends Event> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    public <T extends Event> void publish(T event) {
        List<EventListener> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            // 复制列表以避免并发修改
            for (EventListener listener : new ArrayList<>(eventListeners)) {
                listener.onEvent(event);
            }
        }
    }
}
