package base.tests;

import base.Processor;
import base.ProcessorException;
import base.ProcessorsRunner;

import java.util.*;

public class Test {
    static HashSet<Processor<Integer>> processors = new HashSet<>();

    public static void main(String[] args) throws ProcessorException, InterruptedException {
        processors.add(new ProcessorExecutor<Integer>(new LinkedList<String>(), "0", 0));
        processors.add(new ProcessorExecutor<Integer>(new LinkedList<String>(), "1", 1));
        processors.add(new ProcessorExecutor<Integer>(new LinkedList<String>(), "2", 2));
        processors.add(new ProcessorExecutor<Integer>(new LinkedList<String>(Arrays.asList("1", "2", "5")), "3", 0));
        processors.add(new ProcessorExecutor<Integer>(new LinkedList<String>(Arrays.asList("0", "1")), "4", 0));
        processors.add(new ProcessorExecutor<Integer>(new LinkedList<String>(Arrays.asList("1", "2", "4")), "5", 0));
        processors.add(new ProcessorExecutor<Integer>(new LinkedList<String>(Arrays.asList("0")), "6", 0));
        processors.add(new ProcessorExecutor<Integer>(new LinkedList<String>(Arrays.asList("1", "6")), "7", 0));
        processors.add(new ProcessorExecutor<Integer>(new LinkedList<String>(Arrays.asList("0", "1")), "8", 0));
        processors.add(new ProcessorExecutor<Integer>(new LinkedList<String>(Arrays.asList("1", "2", "4", "5", "7")), "9", 0));
        processors.add(new ProcessorExecutor<Integer>(new LinkedList<String>(), "10", 0));
        processors.add(new ProcessorExecutor<Integer>(new LinkedList<String>(), "11", 0));
        processors.add(new ProcessorExecutor<Integer>(new LinkedList<String>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "11")), "12", 0));


        Map<String, List<Integer>> result =  new ProcessorsRunner<Integer>().runProcessors(
                 processors, 10, 10);

        for (int i = 0; i < 10; i++) {
            for (Integer j = 0; j < 13; j++) {
                System.out.print(result.get(j.toString()).get(i) + " ");
            }
            System.out.println();
        }
    }
}
