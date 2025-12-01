package lab1.application.event;

public interface EventListener<T extends Event> {
    void onEvent(T event);
}
