package tests;

import base.Train;
import java.util.ArrayList;
import java.util.Random;

public class RandomGenerator {
    public static ArrayList<Train> generate() {
        ArrayList<Train> trains = new ArrayList<>();
        Random rand = new Random();
        int num = Math.abs(rand.nextInt(20));

        for (int i = 1; i <= num; i++) {
            trains.add(new Train(i, Math.abs(rand.nextInt()), Math.abs(rand.nextInt()), Math.abs(rand.nextInt())));
        }

        return trains;
    }
}
