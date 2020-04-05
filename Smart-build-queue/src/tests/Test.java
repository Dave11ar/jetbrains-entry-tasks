package tests;

import java.util.InputMismatchException;

public class Test {
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new InputMismatchException("Only one argument allowed --- number of tests");
        }

        try {
            Checker.check(Integer.parseInt(args[0]));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Argument should be integer");
        }
    }
}
