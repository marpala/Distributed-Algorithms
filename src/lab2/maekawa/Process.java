package lab2.maekawa;

import lab2.maekawa.message.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Process extends Remote {
    void receive(Message msg) throws RemoteException;
    int getPid() throws RemoteException;
}
