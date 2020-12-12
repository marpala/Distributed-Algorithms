package lab1.totalorder.message;

import java.io.Serializable;

public class Message implements Comparable<Message>, Serializable {
    private static final long serialVersionUID = 7526471155622776147L;
    private int timestamp; // timestamp of the sending event
    private int processId; // ID of the sending process

    public Message(int timestamp, int processId) {
        this.timestamp = timestamp;
        this.processId = processId;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getProcessId() {
        return processId;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            var other = (Message) obj;
            if (timestamp == other.timestamp && processId == other.processId) {
                return true;
            } else return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int compareTo(Message other) {
        if ((timestamp < other.timestamp) || (timestamp == other.timestamp && processId < other.processId))
            return -1;
        if (this.equals(other))
            return 0;
        return 1;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + timestamp;
        hash = 31 * hash + processId;
        return hash;
    }

    @Override
    public String toString() {
        return "Msg{" +
                "ts=" + timestamp +
                ", pid=" + processId +
                '}';
    }
}
