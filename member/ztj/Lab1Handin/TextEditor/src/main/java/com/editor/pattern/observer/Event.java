package com.editor.pattern.observer;

import java.util.Map;
import java.util.HashMap;

/**
 * 事件类
 * 包含事件类型和附加数据
 */
public class Event {
    private String type;
    private Map<String, Object> data;
    
    public Event(String type) {
        this.type = type;
        this.data = new HashMap<>();
    }
    
    public Event(String type, Object payload) {
        this.type = type;
        this.data = new HashMap<>();
        this.data.put("payload", payload);
    }
    
    public String getType() {
        return type;
    }
    
    public Object getData(String key) {
        return data.get(key);
    }
    
    public void setData(String key, Object value) {
        this.data.put(key, value);
    }
    
    public Object getPayload() {
        return data.get("payload");
    }
    
    @Override
    public String toString() {
        return "Event{type='" + type + "', data=" + data + "}";
    }
}

