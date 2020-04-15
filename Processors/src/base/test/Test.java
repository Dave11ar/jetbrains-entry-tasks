package base.tests;

import base.Processor;
import base.ProcessorException;
import base.MultiThreadSolution;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class Test {
    private static Set<Processor<Long>> processors;
    private static Map<String, List<Long>> resultsMultiThread = new HashMap<>();
    private static Map<String, List<Long>> resultsSingleThread = new HashMap<>();
    private static Map<String, Processor<Long>> idToProcessor = new HashMap<>();
    private static Map<String, Long> startValues = new HashMap<>();

    private static int test;
    private static int passed;

    public static void main(String[] args) throws InterruptedException {
        test = 1;
        passed = 0;

        // if loop == 0, test won't contains dependencies loop
        for (int loop = 0; loop < 2; loop++) {
            for (int numberOfProcessors = 10; numberOfProcessors < 150; numberOfProcessors++) {
                for (int numberOfIterations = 3; numberOfIterations < 30; numberOfIterations++) {

                    fillProcessors(numberOfProcessors);
                    getIdProcessorMap();

                    if (loop == 0) {
                        deleteLoops();
                    }

                    Set<Processor<Long>> processorsCopy = new HashSet<>();
                    for (Processor<Long> curProcessor : processors) {
                        processorsCopy.add(new TestProcessorExecutor<>(List.copyOf(curProcessor.getInputIds()),
                                String.copyValueOf(curProcessor.getId().toCharArray()), startValues.get(curProcessor.getId())));
                    }

                    boolean exceptionSingle = false;
                    boolean exceptionMulti = false;
                    try {
                        resultsSingleThread = new SingleThreadSolution<Long>().runProcessors(processors,
                                0, numberOfIterations);
                    } catch (ProcessorException e) {
                        exceptionSingle = true;
                    }
                    try {
                        resultsMultiThread = new MultiThreadSolution<Long>().runProcessors(processorsCopy,
                                Integer.max((int)Math.abs(Math.random() * 200), 1), numberOfIterations);
                    } catch (ProcessorException e) {
                        exceptionMulti = true;
                    } catch (ExecutionException e) { // shouldn't be in correct program
                        exceptionSingle = false;
                        exceptionMulti = true;
                    }

                    if (exceptionSingle || exceptionMulti) {
                        testResult(exceptionSingle && exceptionMulti);
                        continue;
                    }

                    testResult(check());

                    startValues.clear();
                }
            }
        }

        System.out.println("Passed " + passed + " of " + (test - 1));
    }

    private static void testResult(boolean result) {
        System.out.println("Test #" + test++ + (result ? " passed" : " failed"));
        if (result) {
            passed++;
        }
    }

    /**
     * @return true if  single thread and multi thread results are equals, false otherwise
     */
    private static boolean check() {
        for (Processor<Long> cur : processors) {
            if (resultsMultiThread.get(cur.getId()).size() != resultsSingleThread.get(cur.getId()).size()) {
                return false;
            }

            for (int i = 0; i < resultsSingleThread.get(cur.getId()).size(); i++) {
                if (!resultsSingleThread.get(cur.getId()).get(i).equals(resultsMultiThread.get(cur.getId()).get(i))) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Generate map of processors with random dependencies and startValues

     * @param numberOfProcessors --- number of processors in generated test
     */
    private static void fillProcessors(int numberOfProcessors) {
        processors = new HashSet<>();

        for (Integer curProcessor = 0; curProcessor < numberOfProcessors; curProcessor++) {
            List<String> dependencies = new LinkedList<>();
            Set<String> alreadyIn = new HashSet<>();
            int numberOfDependencies = (int) (Math.random() * numberOfProcessors);

            for (int j = 0; j < numberOfDependencies; j++) {
                Long cur = (long) (Math.random() * numberOfProcessors);
                if (cur.equals((long)curProcessor) || alreadyIn.contains(cur.toString())) {
                    continue;
                }

                alreadyIn.add(cur.toString());
                dependencies.add(cur.toString());
            }

            startValues.put(curProcessor.toString(), (long) (Math.random()) * 100);
            processors.add(new TestProcessorExecutor<>(dependencies, curProcessor.toString(),
                    startValues.get(curProcessor.toString())));
        }
    }

    /**
     * Delete all loops from dependence graph via dfs
     */
    private static void deleteLoops() {
        used = new HashSet<>();
        painted = new HashSet<>();

        for (Processor<Long> processor : processors) {
            if (!used.contains(processor.getId())) {
                dfs(processor);
            }
        }
    }

    private static Set<String> used = new HashSet<>();
    private static Set<String> painted = new HashSet<>();

    private static void dfs(Processor<Long> curProcessor) {
        String processorId = curProcessor.getId();
        List<String> dependencies = List.copyOf(curProcessor.getInputIds());

        used.add(processorId);
        painted.add(processorId);

        for (String edge : dependencies) {
            if (painted.contains(edge)) {
                curProcessor.getInputIds().remove(edge);
                continue;
            }
            if (!used.contains(edge)) {
                dfs(idToProcessor.get(edge));
            }
        }

        painted.remove(processorId);
    }

    private static void getIdProcessorMap() {
        idToProcessor = new HashMap<>();
        for (Processor<Long> processor : processors) {
            idToProcessor.put(processor.getId(), processor);
        }
    }
}
