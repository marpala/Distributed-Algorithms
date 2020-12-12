package lab2.maekawa;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;


/*
[x] TODO Runtime creation of request sets (optimal sets)
[x] TODO Runtime creation of request sets (sub-optimal sets)
[x] TODO Multiple iterations (take care of race conditions within a process)
[x] TODO Random sequence of REQUESTs
[x] TODO better message output
 */

public class Main {
    public static final String MAIN_SERVER_LOC = "localhost";

    public Main() {
        try {
            LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
        } catch (Exception e) {

        }
    }

    public void run(int i, List<Integer> requestSet, int nIter) throws RemoteException, MalformedURLException {
        Process stub = new ProcessImpl(i, requestSet, nIter, MAIN_SERVER_LOC);
        String name = String.format("Process_%d", ((ProcessImpl)stub).getPid());
        Naming.rebind(String.format("rmi://localhost:1099/Process_%d", i), stub);

        try {
            Thread.sleep(1000 + (long)(Math.random() * 500));
            ((ProcessImpl) stub).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param args [0] holds the number of processes
     * @param args [1] holds the id of the current process
     * @param args [2] holds the list of broadcasts
     */
    public static void main(String[] args) {
        int nProcesses = Integer.parseInt(args[0]);
        int pid = Integer.parseInt(args[1]);
        int nIter = Integer.parseInt(args[2]);
        Map<Integer, List<Integer>> requestSet = RequestSetGen.generate(nProcesses);

        try {
            (new Main()).run(pid, requestSet.get(pid), nIter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
