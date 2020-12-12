package lab2.maekawa;

import java.util.*;

public class RequestSetGen {
    private static Map<Integer, List<Integer>> requestSet = null;

    // Generate all combinations ---------
    // https://stackoverflow.com/questions/47325115/java-find-all-combinations-of-given-numbers-in-a-given-array-size-and-store-in
    private static ArrayList<Integer> getSubset(int[] input, int[] subset) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < subset.length; i++)
            result.add(input[subset[i]]);
        return result;
    }

    private static List<ArrayList<Integer>> combinations(int n, int k) {
        List<ArrayList<Integer>> subsets = new ArrayList<>();
        int[] input = new int[n];
        for (int i = 0; i < n; i++) {
            input[i] = i;
        }

        int[] s = new int[k];                  // here we'll keep indices
        // pointing to elements in input array

        if (k <= n) {
            // first index sequence: 0, 1, 2, ...
            for (int i = 0; (s[i] = i) < k - 1; i++);
            subsets.add(getSubset(input, s));
            for(;;) {
                int i;
                // find position of item that can be incremented
                for (i = k - 1; i >= 0 && s[i] == n - k + i; i--);
                if (i < 0) {
                    break;
                }
                s[i]++;                    // increment this item
                for (++i; i < k; i++) {    // fill up remaining items
                    s[i] = s[i - 1] + 1;
                }
                subsets.add(getSubset(input, s));
            }
        }
        return subsets;
    }
    // ----------------

    private static boolean isWhole(double d) {
        return (d == Math.floor(d)) && !Double.isInfinite(d);
    }

    private static int solveQuadratic(int a, int b, int c) {
        double solution = (-b + Math.sqrt(Math.pow(b, 2) - 4*a*c))/(2*a);
        if (isWhole(solution))
            return (int)solution;
        else
            return -1;
    }

    private static int findK(int nProcesses) {
        return solveQuadratic(1, -1, 1 - nProcesses);
    }

    private static Map<Integer, List<Integer>> computeOptimal(int n, int k) {
        Map<Integer, List<Integer>> result = new HashMap<>();
        var combs = combinations(n, k);
        List<List<Integer>> subsets = new ArrayList<>();
        subsets.add(combs.get(0));

        for (int i = 1; i < combs.size(); i++) {
            boolean flag = true;
            for (var s : subsets) {
                var copy = new ArrayList<>(combs.get(i));
                copy.retainAll(s);
                if (copy.size() != 1) {
                    flag = false;
                    break;
                }
            }
            if (flag)
                subsets.add(combs.get(i));
        }

        ArrayList<Integer> remaining = new ArrayList<>();

        while (result.size() != n) {
            for (int i = 0; i < n; i++){
                remaining.add(i);
            }
            for (var s : subsets) {
                for (Integer i : remaining) {
                    if (s.contains(i)) {
                        result.put(i, s);
                        remaining.remove(i);
                        break;
                    }
                }
            }
        }

        return result;
    }

    private static Map<Integer, List<Integer>> computeSubOptimal(int n) {
        int k = (int) Math.ceil(Math.sqrt(n));
        return computeOptimal(n, k);
    }

    public static Map<Integer, List<Integer>> generate(int nProcesses) {
        Map<Integer, List<Integer>> requestSet;
        int k = findK(nProcesses);

        if (k != -1) {
            requestSet = computeOptimal(nProcesses, k);
            return requestSet;
        } else {
            requestSet = computeSubOptimal(nProcesses);
            return requestSet;
        }
    }
}
