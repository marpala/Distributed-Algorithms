package lab1.totalorder;

import lab1.totalorder.message.Ack;
import lab1.totalorder.message.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Process extends Remote {
    void receive(Message msg) throws RemoteException;
    void receive(Ack ack) throws RemoteException;
}
