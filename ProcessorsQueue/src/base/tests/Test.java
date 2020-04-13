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
        processors.add(new ProcessorExecutor<Integer>(new LinkedList<String>(Arrays.asList("1", "2")), "3", 0));
        processors.add(new ProcessorExecutor<Integer>(new LinkedList<String>(Arrays.asList("0", "1")), "4", 0));

        Map<String, List<Integer>> result =  new ProcessorsRunner<Integer>().runProcessors(
                 processors, 10, 10);

        for (int i = 0; i < 10; i++) {
            for (Integer j = 0; j < 5; j++) {
                System.out.print(result.get(j.toString()).get(i) + " ");
            }
            System.out.println();
        }
    }
//    Map<String, List<Integer>> result =  new ProcessorsRunner<Integer>().runProcessors(
//            Set<Processor<Integer>> processors, int maxThreads, int maxIterations) throws ProcessorException;
}
