import { showDirTree } from '../../src/utils/TreeViewer';
import * as fs from 'fs';
import * as path from 'path';

describe('VisualTreeViewer', () => {
    it('should display directory tree structure', () => {
        // Setup: create a temporary directory structure
        const tempDir = path.join(__dirname, 'test/.testfile/TreeViewer');
        fs.mkdirSync(tempDir, { recursive: true });
        fs.writeFileSync(path.join(tempDir, 'file1.txt'), 'Hello World');
        fs.mkdirSync(path.join(tempDir, 'subdir'));
        fs.writeFileSync(path.join(tempDir, 'subdir', 'file2.txt'), 'Hello Subdir');

        // Capture console output
        const consoleSpy = jest.spyOn(process.stdout, 'write').mockImplementation(() => true);
        // Execute
        showDirTree(tempDir);

        // Verify
        const output = consoleSpy.mock.calls.map(call => call[0]).join('\n');
        expect(output).toContain('file1.txt');
        expect(output).toContain('subdir');
        expect(output).toContain('file2.txt');

        // Cleanup
        fs.rmSync(path.join(__dirname, 'test/.testfile/TreeViewer'), { recursive: true, force: true });
        consoleSpy.mockRestore();
    });
});
