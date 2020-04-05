package base;

import java.util.*;

public class Solution {
    private ArrayList<Train> trains;

    public Solution(ArrayList<Train> trains) {
        this.trains = trains;
    }

    public Long solve() {
        /*
        First of all we sort trains by arrival time.
        Main idea of algorithm --- we go from first train to last and on each step we have 2 options:
            1) Skip this train and go to next with 0 of money.
            2) Unload this train and skip next N trains which arrival time < arrival time of current train +
               unloading time and get money for unloading this train.

        So we can use method of dynamic programming and count answer for prefixes of trains.
        We will keep for each train, trains where we can come from and cost of it.
        */

        ArrayList<ArrayList<Pair>> edges = new ArrayList<>(); // array of trains we can come from to i train and cost of it
        long[] dp = new long[trains.size() + 1]; // array of optimal answer for first i trains
        Collections.sort(trains); // sort trains by arrival time

        // initialize edges with start value
        for (int i = 0; i <= trains.size(); i++) {
            edges.add(new ArrayList<>());
        }

        // fill edges with correct values
        for (int i = 0; i < trains.size(); i++) {
            // 1) skip current train and go to next with 0 money
            edges.get(i + 1).add(new Pair(i, 0));
            // 2) unload current train, get unloadingCost and skip next N trains(find it with lowerBound)
            edges.get(lowerBound(i, trains.size(),
                    trains.get(i).arrivalTime + trains.get(i).unloadingTime)).add(
                    new Pair(i, trains.get(i).unloadingCost));
        }

        // correct answer for empty prefix is 0, so we start count next prefixes with invariant that previous prefixes
        // is correct
        for (int i = 1; i < dp.length; i++) {
            // we try all trains where we can come from and take max amount of money, so answer for every prefix is correct
            for (int j = 0; j < edges.get(i).size(); j++) {
                dp[i] = Math.max(dp[i], dp[edges.get(i).get(j).from] + edges.get(i).get(j).cost);
            }
        }

        return dp[trains.size()];
    }

    private int lowerBound(int l, int r, int time) {
        while (l < r - 1) {
            int mid = (l + r) / 2;

            if (trains.get(mid).arrivalTime <  time) {
                l = mid;
            } else {
                r = mid;
            }
        }

        return r;
    }
}
