package lab1.totalorder;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

// TODO is it truly distributed?
// TODO are timestamps and logs printed at the correct time?
public class Main {
    public final int N_PROCESSES;
    public static final String MAIN_SERVER_LOC = "localhost";
    public List<Integer> test;


    //    TODO see correctness of 4.1
    public Main(int n_PROCESSES, List<Integer> test) {
        N_PROCESSES = n_PROCESSES;
        this.test = test;
        try {
            LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
        } catch (Exception e) {

        }
    }

    public void run(int i) throws RemoteException, MalformedURLException {
        Process stub = new ProcessImpl(i, N_PROCESSES, test.size(), MAIN_SERVER_LOC);
        String name = String.format("Process_%d", ((ProcessImpl)stub).getId());
        Naming.rebind(String.format("rmi://localhost:1099/Process_%d", i), stub);

        try {
            Thread.sleep(1000 + (long)(Math.random() * 500));
            ((ProcessImpl) stub).run(test);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Integer> parseString(String input) {
        String[] s = input.split(",");
        List<Integer> out = new ArrayList<>(s.length);
        for(int i = 0 ; i < s.length ; i++)
            out.add(Integer.parseInt(s[i]));
        return out;
    }

    /**
     *
     * @param args [0] holds the number of processes
     * @param args [1] holds the id of the current process
     * @param args [2] holds the list of broadcasts
     */
    public static void main(String[] args) {
        int nProcesses = Integer.parseInt(args[0]);
        int currentProcess = Integer.parseInt(args[1]);
        List<Integer> test = parseString(args[2]);
        try {
            (new Main(nProcesses, test)).run(currentProcess);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
