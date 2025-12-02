package com.editor.util;

/**
 * 位置类
 * 表示文本中的行列位置
 */
public class Position {
    private int line;
    private int column;
    
    public Position(int line, int column) {
        this.line = line;
        this.column = column;
    }
    
    public int getLine() {
        return line;
    }
    
    public void setLine(int line) {
        this.line = line;
    }
    
    public int getColumn() {
        return column;
    }
    
    public void setColumn(int column) {
        this.column = column;
    }
    
    @Override
    public String toString() {
        return "Position{line=" + line + ", column=" + column + "}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return line == position.line && column == position.column;
    }
    
    @Override
    public int hashCode() {
        return line * 31 + column;
    }
}

