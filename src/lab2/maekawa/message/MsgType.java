package lab2.maekawa.message;


public enum MsgType {
    REQUEST,
    INQUIRE,
    GRANT, // LOCKED
    RELEASE,
    POSTPONED, // WAITING QUEUE
    RELINQUISH
}
