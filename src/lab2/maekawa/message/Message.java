package lab2.maekawa.message;

import java.io.Serializable;

public class Message implements Comparable<Message>, Serializable {
    private static final long serialVersionUID = 7526471155622776147L;
    private final int seq; // seq of the sending event
    private final int pid; // ID of the sending process
    private final MsgType type;

    public Message(MsgType type, int seq, int pid) {
        this.type = type;
        this.seq = seq;
        this.pid = pid;
    }

    public Message(MsgType type, int pid) {
        this.type = type;
        this.pid = pid;
        this.seq = -1;
    }

    public MsgType getType() {
        return type;
    }

    public int getSeq() {
        return seq;
    }

    public int getPid() {
        return pid;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            var other = (Message) obj;
            if (seq == other.seq && pid == other.pid) {
                return true;
            } else return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int compareTo(Message other) {
        if ((seq < other.seq) || (seq == other.seq && pid < other.pid))
            return -1;
        if (this.equals(other))
            return 0;
        return 1;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + seq;
        hash = 31 * hash + pid;
        return hash;
    }

    @Override
    public String toString() {
        if (seq < 0) {
            return type + "{" +
                    "pid=" + pid +
                    '}';
        }
        return type + "{" +
                "seq=" + seq +
                ", pid=" + pid +
                '}';
    }
}
