package tests;

import base.Solution;
import base.Train;
import java.util.ArrayList;
import java.util.Collections;

public class Checker {
    public static void check(int numOfTests) {
        int correctAns = 0;
        for (int i = 0; i < numOfTests; i++) {
            ArrayList<Train> trains = RandomGenerator.generate();

            long correct = calculate(trains);
            long yourAns = new Solution(trains).solve();
            System.out.println("Correct answer: " + correct + "\nYour answer:    " + yourAns);
            if (correct == yourAns) {
                System.out.println("OK");
                correctAns++;
            } else {
                System.out.println("WA");
            }
        }

        System.out.println("::::::::::::::::::::::::::");
        System.out.println("Passed " + correctAns + " of " + numOfTests);
    }

    /*
    Naive algorithm working for O(2^n).
    We sort trains by arrive time and using bitmask try all sequences of unloading them.
    */
    public static Long calculate(ArrayList<Train> trains) {
        long ans = 0;

        Collections.sort(trains);
        for (long mask = 0; mask < (1 << trains.size()); mask++) {
            ArrayList<Train> currentTrains = new ArrayList<>();

            for (int train = 0; train < trains.size(); train++) {
                long take = (mask >> train) & 1; // 1 on i position means that we take this train
                if (take == 1) {
                    currentTrains.add(trains.get(train));
                }
            }

            int readyTime = 0; // time when we can start to unload next train
            long currentAns = 0; // answer in this sequence of trains
            for (Train currentTrain : currentTrains) {
                if (readyTime > currentTrain.arrivalTime) {
                    currentAns = 0;
                    break;
                }

                currentAns += currentTrain.unloadingCost;
                readyTime = currentTrain.arrivalTime + currentTrain.unloadingTime;
            }

            ans = Math.max(ans, currentAns);
        }

        return ans;
    }
}
