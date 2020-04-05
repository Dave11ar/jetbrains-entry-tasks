package base;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Scanner input = new Scanner(new InputStreamReader(new FileInputStream(args[0]), StandardCharsets.UTF_8));
            PrintWriter output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[1]), StandardCharsets.UTF_8));

            Solution solution = new Solution();
            solution.setSource(input, output);
            solution.solve();
            input.close();
            output.close();
        } catch (FileNotFoundException e) {

        }
    }
}
