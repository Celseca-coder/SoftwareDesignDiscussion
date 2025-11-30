export type Event = string;
export type EventPayload = any;
export type EventHandler = (payload: EventPayload) => void;

export class EventBus {
    private handlers = new Map<Event, EventHandler[]>();

    on(event: Event, handler: EventHandler){
        const list = this.handlers.get(event) ?? [];
        list.push(handler);
        this.handlers.set(event, list);
    }
    
    off(event: Event, handler: EventHandler){
        const list = this.handlers.get(event) ?? [];
        this.handlers.set(event, list.filter(h => h !== handler));
    }

    emit(event: Event, payload?: EventPayload){
        const list = this.handlers.get(event) ?? [];
        for (const callback of list){
            try {
                callback(payload);
            } catch (err) {
                console.error(event + " handler error", err);
            }
        }
    }
}
