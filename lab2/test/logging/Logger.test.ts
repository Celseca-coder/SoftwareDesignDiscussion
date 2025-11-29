import { EventBus } from '../../src/core/EventBus';
import { Logger } from '../../src/logging/Logger';
import * as fs from 'fs';

describe('Logger', () => {
    let eventBus: EventBus;
    let logger: Logger;
    const testEditorInfo = {
        id: 'test-editor',
        path: 'test/.testfile/logger-test.txt',
        modified: true,
        loggingEnabled: true
    };
    const logPath = 'test/.testfile/.logger-test.txt.log';

    beforeEach(() => {
        eventBus = new EventBus();
        logger = new Logger(eventBus);
    });

    afterEach(() => {
        if (fs.existsSync(logPath)) fs.unlinkSync(logPath);
    });

    it('should log editor start event', () => {
        const logMessage = 'Editor started';
        eventBus.emit('editor-start', { editorInfo: testEditorInfo, log: logMessage });
        const logContent = fs.readFileSync(logPath, 'utf-8');
        expect(logContent).toContain(logMessage);
    });

    it('should log command execute event', () => {
        const logMessage = 'Command executed';
        eventBus.emit('command-execute', { editorInfo: testEditorInfo, log: logMessage });
        const logContent = fs.readFileSync(logPath, 'utf-8');
        expect(logContent).toContain(logMessage);
    });

    it('should show log content', () => {
        const logMessage = 'Test log content';
        fs.writeFileSync(logPath, logMessage, 'utf-8');
        console.log = jest.fn();
        logger.showLog(testEditorInfo.path);
        expect(console.log).toHaveBeenCalledWith(logMessage);
    });

    it('should warn if log file does not exist', () => {
        console.warn = jest.fn();
        logger.showLog(testEditorInfo.path);
        expect(console.warn).toHaveBeenCalled();
    });

    it('should not log if logging is disabled', () => {
        const disabledEditorInfo = { ...testEditorInfo, loggingEnabled: false };
        const logMessage = 'This should not be logged';
        eventBus.emit('editor-start', { editorInfo: disabledEditorInfo, log: logMessage });
        expect(fs.existsSync(logPath)).toBe(false);
    });
});
