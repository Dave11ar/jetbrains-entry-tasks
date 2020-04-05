package base;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 2) {
            throw new InputMismatchException("number of args should be 2");
        }

        try {
            Scanner input = new Scanner(new InputStreamReader(new FileInputStream(args[0]), StandardCharsets.UTF_8));
            PrintWriter output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[1]), StandardCharsets.UTF_8));

            output.write(new Solution(read(input)).solve().toString());
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("could not find file");
        }
    }

    public static ArrayList<Train> read(Scanner input) {
        ArrayList<Train> trains = new ArrayList<>();
        int numberOfTrains;
        numberOfTrains = input.nextInt();

        for (int i = 0; i < numberOfTrains; i++) {
            trains.add(new Train(input.nextInt(), input.nextInt(),
                    input.nextInt(), input.nextInt()));
        }
        input.close();

        return trains;
    }
}
