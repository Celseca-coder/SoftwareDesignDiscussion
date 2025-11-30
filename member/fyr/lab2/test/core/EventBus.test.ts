import { EventBus } from '../../src/core/EventBus';

describe('EventBus', () => {
    let bus: EventBus;

    beforeEach(() => {
        bus = new EventBus();
    });

    it('should register and emit events', () => {
        const handler = jest.fn();
        bus.on('testEvent', handler);
        bus.emit('testEvent', { data: 123 });
        expect(handler).toHaveBeenCalledWith({ data: 123 });
    });

    it('should handle multiple handlers for the same event', () => {
        const handler1 = jest.fn();
        const handler2 = jest.fn();
        bus.on('testEvent', handler1);
        bus.on('testEvent', handler2);
        bus.emit('testEvent', { data: 456 });
        expect(handler1).toHaveBeenCalledWith({ data: 456 });
        expect(handler2).toHaveBeenCalledWith({ data: 456 });
    });

    it('should unregister event handlers', () => {
        const handler = jest.fn();
        bus.on('testEvent', handler);
        bus.off('testEvent', handler);
        bus.emit('testEvent', { data: 123 });
        expect(handler).not.toHaveBeenCalled();
    });
});
