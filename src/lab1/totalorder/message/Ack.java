package lab1.totalorder.message;

public class Ack extends Message {
    /**
     * The acknowledge lab1.totalorder.message
     */
    private Message msg;

    public Ack(Message msg, int timestamp, int processId) {
        super(timestamp, processId);
        this.msg = msg;
    }

    public Message getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "Ack{" +
                msg +
                ", ts=" + this.getTimestamp() +
                ", pid=" + this.getProcessId() +
                '}';
    }
}
