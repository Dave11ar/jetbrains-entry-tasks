package base;

import java.io.PrintWriter;
import java.util.*;

public class Solution {
    private Scanner input;
    private PrintWriter output;
    private ArrayList<Train> trains = new ArrayList<>();

    public void solve() {
        read();
        ArrayList<ArrayList<Pair>> edges = new ArrayList<>();
        ArrayList<Integer> dp = new ArrayList<>();
        Collections.sort(trains);

        for (int i = 0; i <= trains.size(); i++) {
            edges.add(new ArrayList<Pair>());
            dp.add(0);
        }

        for (int i = 0; i < trains.size(); i++) {
            edges.get(i + 1).add(new Pair(i, 0));
            edges.get(lowerBound(i, trains.size(),
                    trains.get(i).arrivalTime + trains.get(i).unloadingTime)).add(
                    new Pair(i, trains.get(i).unloadingCost));
        }

        for (int i = 1; i < dp.size(); i++) {
            for (int j = 0; j < edges.get(i).size(); j++) {
                dp.set(i,  Integer.max(dp.get(i), dp.get(edges.get(i).get(j).to) + edges.get(i).get(j).weight));
            }
        }

        write(dp.get(trains.size()));
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

    private void read() {
        int numberOfTrains;
        numberOfTrains = input.nextInt();

        for (int i = 0; i < numberOfTrains; i++) {
            trains.add(new Train(input.nextInt(), input.nextInt(),
                    input.nextInt(), input.nextInt()));
        }
    }

    private void write(Integer answer) {
        output.write(answer.toString());
    }

    public void setSource(Scanner input, PrintWriter output) {
        this.input = input;
        this.output = output;
    }
}
