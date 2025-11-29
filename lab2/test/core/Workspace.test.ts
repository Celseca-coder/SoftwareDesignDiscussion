import { Workspace } from '../../src/core/Workspace';

jest.mock('../../src/utils/input', () => ({
  rl: {
    question: jest.fn().mockResolvedValue('y'), // 默认回答 'y'
    close: jest.fn(),
  }
}));

jest.mock('../../src/core/EventBus', () => ({
  EventBus: jest.fn().mockImplementation(() => ({
    emit: jest.fn(),
    on: jest.fn(),
  })),
}));

jest.mock('../../src/logging/Logger', () => ({
  Logger: jest.fn().mockImplementation(() => ({
    showLog: jest.fn(),
  })),
}));

// TextEditor 模拟：create() 返回带基本结构的对象
const mockEditors: any[] = [];
jest.mock('../../src/editor/TextEditor', () => ({
  TextEditor: {
    create: jest.fn((file: string) => {
      const editor = {
        info: {
          id: 'ed-' + Math.random().toString(36).slice(2, 6),
          path: file,
          modified: false,
          loggingEnabled: false,
        },
        save: jest.fn(),
        undoCommand: jest.fn(),
        redoCommand: jest.fn(),
        executeCommand: jest.fn(),
        getContent: jest.fn(() => 'file content'),
        appendLine: jest.fn(),
      };
      mockEditors.push(editor);
      return editor;
    }),
  },
}));

// WorkspaceMemento 模拟，避免真的写文件
jest.mock('../../src/core/WorkspaceMemento', () => ({
  WorkspaceMemento: {
    load: jest.fn().mockReturnValue(null),
  },
}));

// 模拟 fs
jest.mock('fs');

describe('Workspace', () => {
  let ws: Workspace;
  let mockReadline: any;

  beforeEach(() => {
    ws = new Workspace();
  });

  afterEach(() => {
    jest.clearAllMocks();
    mockEditors.length = 0;
  });

  it('should load() creates a new editor and sets it active', () => {
    ws.load('a.txt');
    expect(mockEditors.length).toBe(1);
    const editor = mockEditors[0];
    expect(editor.info.path).toBe('a.txt');
  });

  it('should load() twice on same file does not create duplicate', () => {
    ws.load('a.txt');
    ws.load('a.txt');
    expect(mockEditors.length).toBe(1); // only one editor created
  });

  it('should save("all") calls save() on all editors', () => {
    ws.load('a.txt');
    ws.load('b.txt');
    ws.save('all');
    expect(mockEditors[0].save).toHaveBeenCalled();
    expect(mockEditors[1].save).toHaveBeenCalled();
  });

  it('should close() calls confirmYesNo() and save() when confirmed', async () => {
    ws.load('a.txt');
    const editor = mockEditors[0];
    editor.info.modified = true;

    await ws.close(undefined); // close active
    expect(editor.save).toHaveBeenCalled();
  });

  it('should undo() and redo() call correct editor methods', () => {
    ws.load('a.txt');
    const editor = mockEditors[0];
    ws.undo();
    ws.redo();
    expect(editor.undoCommand).toHaveBeenCalled();
    expect(editor.redoCommand).toHaveBeenCalled();
  });

  it('should logOn() and logOff() toggle loggingEnabled flag', () => {
    ws.load('a.txt');
    const editor = mockEditors[0];
    ws.logOn('a.txt');
    expect(editor.info.loggingEnabled).toBe(true);
    ws.logOff('a.txt');
    expect(editor.info.loggingEnabled).toBe(false);
  });

  it('should editorCommand("append") delegates to editor.executeCommand()', () => {
    ws.load('a.txt');
    const editor = mockEditors[0];
    ws.editorCommand('append', 'test line');
    expect(editor.executeCommand).toHaveBeenCalled();
  });
});
