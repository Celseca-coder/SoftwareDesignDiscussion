package com.editor.logging;

/**
 * 日志管理器
 * 单例模式，提供全局日志访问
 */
public class LogManager {
    private static LogManager instance;
    private Logger logger;
    
    private LogManager() {
        this.logger = new Logger("editor.log", true, LogEvent.LogLevel.INFO);
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    /**
     * 设置自定义日志器
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    
    // 便捷方法
    public static void debug(String message, String source) {
        getInstance().logger.debug(message, source);
    }
    
    public static void info(String message, String source) {
        getInstance().logger.info(message, source);
    }
    
    public static void warn(String message, String source) {
        getInstance().logger.warn(message, source);
    }
    
    public static void error(String message, String source) {
        getInstance().logger.error(message, source);
    }
}

