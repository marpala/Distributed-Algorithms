package lab2.maekawa;

import common.Colors;
import lab2.maekawa.message.Message;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

import lab2.maekawa.message.MsgType;
import static lab2.maekawa.message.MsgType.*;

public class ProcessImpl extends UnicastRemoteObject implements Process {
    private final int pid;
    private final Registry registry;
    private final Queue<Message> outstanding = new PriorityBlockingQueue<>();
    private final List<Integer> requestSet;
    private int nGrants;
    private boolean granted = false;
    private boolean inquiring = false;
    private boolean postponed = false;
    private Message currentGrant;
    private final boolean verbose = false;
    private int seqN = 0;
    private final int nIter;
    private boolean busy = false;


    protected ProcessImpl(int pid, List<Integer> requestSet, int nIter, String registryLoc) throws RemoteException {
        this.pid = pid;
        this.registry = LocateRegistry.getRegistry(registryLoc);
        this.requestSet = requestSet;
        this.nGrants = 0;
        this.currentGrant = new Message(REQUEST, -1, -1);
        this.nIter = nIter;
    }

    private Message newMsg(MsgType type) {
        if (type == REQUEST) {
            return new Message(type, ++seqN, pid);
        }
        return new Message(type, pid);
    }

    private Process getProcessRef(int pid) {
        Process p = null;
        try {
            p = (Process) registry.lookup("Process_" + pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }

    private void sendAll(Message msg) {
        for (Integer i : requestSet) {
            send(msg, i);
        }
    }

    private void send(Message msg, int pidReceiver) {
        Process p = getProcessRef(pidReceiver);
        try {
            randsleep(500);
            p.receive(msg);
            if (verbose)
                System.out.println("Process " + pid + " sent " + msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void request() {
        randsleep(1000);
        busy = true;
        nGrants = 0;
        sendAll(newMsg(REQUEST));
    }

    private void release() {
        sendAll(newMsg(RELEASE));
        busy = false;
    }

    private void receiveRequest(Message msg) {
        seqN = Math.max(seqN + 1, msg.getSeq() + 1);
        if (!granted) {
            currentGrant = msg;
            granted = true;
            send(newMsg(GRANT), msg.getPid());
        } else {
            outstanding.add(msg);
            Message prev = outstanding.peek();
            if (currentGrant.compareTo(msg) < 0 || prev.compareTo(msg) < 0) {
                send(newMsg(POSTPONED), msg.getPid());
            } else if (!inquiring) {
                inquiring = true;
                send(newMsg(INQUIRE), currentGrant.getPid());
            }
        }
    }

    private void receiveGrant(Message msg) {
        nGrants++;
        if (nGrants >= requestSet.size()) {
            postponed = false;
            enterCS();
            release();
        }
    }

    private void receiveInquire(Message msg) {
        while(!postponed && nGrants < requestSet.size()) {
            try {
                Thread.sleep(69);
            } catch (InterruptedException e) {

            }
        }
        if (postponed) {
            nGrants--;
            send(newMsg(RELINQUISH), msg.getPid());
        }
    }

    private void receiveRelinquish(Message msg) {
        inquiring = false;
        granted = false;
        outstanding.add(currentGrant);
        sendGrant();
    }

    private void sendGrant() {
        granted = true;
        currentGrant = outstanding.poll();
        if (!(currentGrant == null)) {
            send(newMsg(GRANT), currentGrant.getPid());
        }
    }

    private void receiveRelease(Message msg) {
        granted = false;
        inquiring = false;
        if (!outstanding.isEmpty()) {
            sendGrant();
        }
    }

    private void receivePostponed(Message msg) {
        postponed = true;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void randsleep(long maxMillis) {
        sleep((long) Math.random() * maxMillis);
    }

    private void enterCS() {
        int time = 1000 + (int)(Math.random() * 1000);
        int dots = 22;
        System.out.println("\n🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨");
        System.out.println(String.format("Process %d has ENTERED its critical section! At time " + System.nanoTime(), pid));
        Thread t = new Thread(() -> {
            for (int i = 0; i < dots; i++) {
                sleep(time/dots);
                System.out.print(" . ");
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(String.format("\nProcess %d has EXITED its critical section! At time " + System.nanoTime(), pid));
        System.out.println("🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨 🚨\n");
    }

    public void run() {
        for (int i = 0; i < nIter; i++) {
                if (Math.random() >= 0.5) {
                while (busy) {
                    sleep(1000);
                }
                request();
            }
        }

        while (granted) {
            sleep(200);
        }
        if (verbose)
            System.out.println(Colors.BLUE + "Process " + pid + " is done! Waiting for others to finish..." + Colors.RESET);
    }

    @Override
    public void receive(Message msg) {
        if (verbose)
            System.out.println("Process " + pid + " received " + msg);
        switch (msg.getType()) {
            case GRANT:
                receiveGrant(msg);
                break;
            case REQUEST:
                receiveRequest(msg);
                break;
            case INQUIRE:
                receiveInquire(msg);
                break;
            case RELEASE:
                receiveRelease(msg);
                break;
            case POSTPONED:
                receivePostponed(msg);
                break;
            case RELINQUISH:
                receiveRelinquish(msg);
                break;
            default:
                System.out.println("The message must have a type!");
        }
    }

    public int getPid() {
        return pid;
    }
}
