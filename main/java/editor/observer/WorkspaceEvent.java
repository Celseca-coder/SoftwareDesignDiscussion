// WorkspaceEvent.java
package editor.observer;

import java.io.Serializable;

/**
 * 工作区事件
 */
public class WorkspaceEvent implements Serializable {
    private String type;
    private String data;
    private long timestamp;

    public WorkspaceEvent(String type, String data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public String getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }
}