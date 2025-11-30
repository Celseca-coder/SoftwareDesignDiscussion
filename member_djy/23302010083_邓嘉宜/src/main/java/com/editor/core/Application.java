package com.editor.core;

/**
 * 应用程序类
 * 管理应用程序状态
 */
public class Application {
    private boolean running = true;
    
    public boolean isRunning() {
        return running;
    }
    
    public void exit() {
        running = false;
    }
}
