package lab1.totalorder;

import lab1.totalorder.message.Ack;
import lab1.totalorder.message.Message;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import common.Colors;

public class ProcessImpl extends UnicastRemoteObject implements Process {
    private final int id;
    private int clock;
    private final int nProcesses;
    private final int nMessages;
    private final Registry registry;
    private final Queue<Message> pendingDelivery;
    private final Map<Message, Integer> remainingAcks;
    private final List<Message> delivered = new ArrayList<>();
    private int nThreads;
    private ExecutorService executor;
    private final boolean verbose = false;

    public ProcessImpl(int id, int nProcesses, int nMessages, String registryLoc) throws RemoteException {
        this.id = id;
        this.clock = 0;
        this.registry = LocateRegistry.getRegistry(registryLoc);
        this.pendingDelivery = new PriorityBlockingQueue<>();
        this.nProcesses = nProcesses;
        this.nMessages = nMessages;
        this.remainingAcks = new HashMap<>();
    }

    private void attemptExit() {
        try {
//            Thread.sleep(1000);
//            System.out.println("Process" + id + " goodbye");
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);
            System.out.println(Colors.BOLD + Colors.GREEN + "Process " + id + " delivered " + delivered + Colors.RESET);
            UnicastRemoteObject.unexportObject(this, true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
        }

    }

    private void deliver() {
        Message msg = pendingDelivery.poll();
        if (msg == null)
            return;

        randSleep(500);
        if (verbose)
            System.out.println(Colors.CYAN + "Process " + id + " delivered message " + msg + "at time " + clock + Colors.RESET);
        remainingAcks.remove(msg);
        delivered.add(msg);
        if (delivered.size() == nMessages)
            attemptExit();
        attemptDelivery(); // try to deliver another lab1.totalorder.message if possible
    }

    private void attemptDelivery() {
        Message msg = pendingDelivery.peek();
        if (msg == null)
            return;
        if (remainingAcks.get(msg) == 0) {
            synchronized (this) {
                deliver();
            }
        }
    }

    public void run(List<Integer> broadcasts) {
        nThreads = Collections.frequency(broadcasts, id) + broadcasts.size();
        executor = Executors.newFixedThreadPool(nThreads);
        for (Integer i : broadcasts) {
            if (i == id) {
                try {
                    randSleep(500);
                    broadcast();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void threadedSendAll(Message msg) {
//        new Thread(() -> sendAll(msg)).start();
        Runnable worker = () -> sendAll(msg);
        executor.execute(worker);
    }

    public void broadcast() {
        synchronized (this) {
            clock++;
        }
        Message msg = new Message(clock, id);
        if (verbose)
            System.out.println(Colors.YELLOW + "Process " + id + " started broadcasting " + msg + "at time " + clock + Colors.RESET);
        threadedSendAll(msg);
    }

    private void broadcast(Ack ack) {
        if (verbose)
            System.out.println(Colors.YELLOW + "Process " + id + " started broadcasting " + ack  + "at time " + clock + Colors.RESET);
        threadedSendAll(ack);
    }

    private void sendAll(Message msg) {
        for (int i = 0; i < nProcesses; i++) {
            try {
                randSleep(1000);
                Process p = (Process) registry.lookup("Process_" + i);
                if (msg instanceof Ack) {
                    p.receive((Ack) msg);
                } else
                    p.receive(msg);
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates a new ACK counter, unless one already exists for the same lab1.totalorder.message
     * @param msg the lab1.totalorder.message for which the counter will be created
     */
    private void newAckCounter(Message msg) {
        if (!remainingAcks.containsKey(msg))
            remainingAcks.put(msg, nProcesses);
    }

    /**
     * Decrease the counter of the remaining acks for the given lab1.totalorder.message
     * @param msg the ack whose lab1.totalorder.message's counter will be decreased
     */
    private void decreaseAckCounter(Message msg) {
        int remaining = remainingAcks.get(msg);
        remainingAcks.put(msg, remaining - 1);
    }

    @Override
    public void receive(Message msg){
        synchronized (this) {
            clock = Math.max(clock + 1, msg.getTimestamp() + 1);
        }
        if (verbose)
            System.out.println(Colors.BLUE + "Process " + id + " received " + msg + "at time " + clock + Colors.RESET);
        pendingDelivery.add(msg);
        newAckCounter(msg);
        try {
            synchronized (this) {
                clock++;
            }
            broadcast(new Ack(msg, clock, id));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receive(Ack ack) {
        synchronized (this) {
            clock = Math.max(clock + 1, ack.getTimestamp() + 1);
        }
        if (verbose)
            System.out.println(common.Colors.PURPLE + "Process " + id + " received " + ack + "at time " + clock + Colors.RESET);
        newAckCounter(ack.getMsg()); // in case the ACK is received before the acknowledged lab1.totalorder.message itself
        decreaseAckCounter(ack.getMsg());
        attemptDelivery();
    }

    private void randSleep(long maxMillis) {
        try {
            Thread.sleep((long)(Math.random() * maxMillis));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }
}
