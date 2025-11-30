package com.editor.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TextEditorTest {
    @Test
    void appendAndCount() {
        TextEditor editor = new TextEditor("dummy");
        editor.appendLine("a");
        editor.appendLine("b");
        assertEquals(2, editor.getLineCount());
        assertEquals("a", editor.getLine(0));
        assertEquals("b", editor.getLine(1));
        assertTrue(editor.isModified());
    }

    @Test
    void insertAndDelete() {
        TextEditor editor = new TextEditor("dummy");
        editor.setLines(Arrays.asList("x", "y"));
        editor.insertLine(1, "z");
        assertEquals(Arrays.asList("x", "z", "y"), editor.getLines());
        editor.deleteLine(0);
        assertEquals(Arrays.asList("z", "y"), editor.getLines());
    }

    @Test
    void setAndGetLine() {
        TextEditor editor = new TextEditor("dummy");
        editor.setLines(Arrays.asList("a", "b"));
        editor.setLine(1, "bb");
        assertEquals("bb", editor.getLine(1));
        assertNull(editor.getLine(5));
    }

    @Test
    void saveAndLoad(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("test.txt");
        TextEditor e1 = new TextEditor(file.toString());
        e1.setLines(Arrays.asList("one", "two"));
        e1.save();
        assertFalse(e1.isModified());

        TextEditor e2 = new TextEditor(file.toString());
        e2.load();
        assertEquals("one\ntwo", e2.getContent());
        assertEquals(2, e2.getLineCount());
    }
}