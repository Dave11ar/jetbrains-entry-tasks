package base.tests;

import base.Processor;
import base.ProcessorException;
import base.MultiThreadSolution;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class Test {
    private static Set<Processor<Long>> processors = new HashSet<>();
    private static  Map<String, List<Long>> processorsThread = new HashMap<>();
    private static  Map<String, List<Long>> processorsSingle = new HashMap<>();
    private static  Map<String, Long> aba = new HashMap<>();

    public static void main(String[] args) throws ProcessorException, InterruptedException {
        int test = 1;
        int passed = 0;

        for (int loop = 0; loop < 2; loop++) {
            for (int i = 10; i < 150; i++) {
                for (int j = 3; j < 30; j++) {
                    fillProcessors(i);
                    getIdProcessorMap();
                    if (loop == 0) {
                        deleteLoops();
                    }

                    Set<Processor<Long>> processorsCopy = new HashSet<>();
                    for (Processor<Long> curProcessor : processors) {
                        processorsCopy.add(new ProcessorExecutor<Long>(List.copyOf(curProcessor.getInputIds()), String.copyValueOf(curProcessor.getId().toCharArray()), aba.get(curProcessor.getId())));
                    }

                    boolean i1 = false;
                    boolean i2 = false;
                    try {
                        processorsSingle = new SingleThreadSolution<Long>().runProcessors(processors, 50, j);
                    } catch (ProcessorException e) {
                        i1 = true;
                    }
                    try {
                        processorsThread = new MultiThreadSolution<Long>().runProcessors(processorsCopy,
                                10/*Integer.max((int)Math.abs(Math.random() * 50), 1)*/, j);
                    } catch (ProcessorException | ExecutionException /*| ExecutionException*/ e) {
                        i2 = true;
                    }
                    //processorsSingle = processorsThread;

                    if (i1 || i2) {
                        if (!i1 || !i2) {
                            System.out.println("Test #" + test + " failed");
                        } else {
                            System.out.println("Test #" + test + " passed");
                            passed++;
                        }
                        test++;
                        continue;
                    }

                    if (check()) {
                        System.out.println("Test #" + test + " passed");
                        passed++;
                    } else {
                        System.out.println("Test #" + test + " failed");
                    }
                    test++;
                    aba.clear();
                }
            }
        }
        System.out.println("Passed " + passed + " of " + (test - 1));
    }

    private static boolean check() {
        for (Processor<Long> cur : processors) {
            if (processorsThread.get(cur.getId()).size() != processorsSingle.get(cur.getId()).size()) {
                return false;
            }

            for (int i = 0; i < processorsSingle.get(cur.getId()).size(); i++) {
                if (!processorsSingle.get(cur.getId()).get(i).equals(processorsThread.get(cur.getId()).get(i))) {
                    return false;
                }
            }
        }

        return true;
    }

    private static void fillProcessors(int numberOfProcessors) {
        processors = new HashSet<>();
        for (Integer i = 0; i < numberOfProcessors; i++) {
            List<String> dependencies = new LinkedList<>();
            Set<String> alreadyIn = new HashSet<>();
            int d = (int) (Math.random() * Math.sqrt(numberOfProcessors));

            for (int j = 0; j < d; j++) {
                Long cur = (long) (Math.random() * numberOfProcessors);
                if (cur.equals((long)i) || alreadyIn.contains(cur.toString())) continue;

                alreadyIn.add(cur.toString());
                dependencies.add(cur.toString());
            }
            aba.put(i.toString(), (long) (Math.abs(Math.random()) * 10));
            processors.add(new ProcessorExecutor<>(dependencies, i.toString(), aba.get(i.toString())));
        }
    }

    private static void deleteLoops() {
        used = new HashSet<>();
        painted = new HashSet<>();
        for (Processor<Long> processor : processors) {
            if (!used.contains(processor.getId())) {
                run(processor);
            }
        }
    }

    private static Set<String> used = new HashSet<>();
    private static Set<String> painted = new HashSet<>();

    private static void run(Processor<Long> curProcessor) {
        String processorId = curProcessor.getId();
        List<String> opa = curProcessor.getInputIds();
        List<String> dependencies = new ArrayList<>();

        for (int i = 0; i < opa.size(); i++) {
            dependencies.add(String.valueOf(opa.get(i).toCharArray()));
        }

        used.add(processorId);
        painted.add(processorId);

        for (int i = 0; i < dependencies.size(); i++) {
            if (painted.contains(dependencies.get(i))) {
                curProcessor.getInputIds().remove(dependencies.get(i));
                continue;
            }
            if (used.contains(dependencies.get(i))) {
                continue;
            }
            run(idToProcessor.get(dependencies.get(i)));
        }
        painted.remove(processorId);

    }


    private static Map<String, Processor<Long>> idToProcessor = new HashMap<>();

    private static void getIdProcessorMap() {
        idToProcessor = new HashMap<>();
        for (Processor<Long> processor : processors) {
            idToProcessor.put(processor.getId(), processor);
        }
    }
}
