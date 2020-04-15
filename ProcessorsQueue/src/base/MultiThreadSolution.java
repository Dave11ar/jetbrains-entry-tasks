package base;

import java.util.*;
import java.util.concurrent.*;

public class MultiThreadSolution<T> implements Runner<T> {
    private Set<Processor<T>> processors;
    private int maxIterations;
    private int nullIteration;
    private Map<String, List<T>> results;
    private Map<String, Processor<T>> idToProcessor = new HashMap<>();
    private Map<String, ArrayList<String>> graphOfDependencies = new HashMap<>();
    private HashMap<String,  Integer> curIteration = new HashMap<>();
    private ExecutorService threadPoolExecutor;
    private int counter = 0;
    private Set<String> gline = new HashSet<>();
    private Map<String, Future<T>>futureTasks = new HashMap<>();
    private boolean opa = false;
    private HashSet<String> used = new HashSet<>();
    private HashSet<String> paintedVertexes = new HashSet<>();
    private ArrayList<String> sourceProcessors = new ArrayList<>();

    @Override
    public Map<String, List<T>> runProcessors(Set<Processor<T>> processors, int maxThreads, int maxIterations)
            throws ProcessorException, InterruptedException, ExecutionException {
        this.nullIteration = maxIterations;
        this.processors = processors;
        this.maxIterations = maxIterations;
        this.results = new HashMap<>();

        for (Processor<T> curProcessor : processors) {
            curIteration.put(curProcessor.getId(), 0);
            results.put(curProcessor.getId(), new LinkedList<>());
        }

        getIdProcessorMap();
        makeDependenciesGraph();

        threadPoolExecutor = Executors.newFixedThreadPool(maxThreads);

        for (String curProcessor : sourceProcessors) {
            gline.add(curProcessor);
            futureTasks.put(curProcessor, threadPoolExecutor.submit((Callable<T>) new ProcessorThread<T>(
                    idToProcessor.get(curProcessor), makeListOfDependencies(curProcessor, curIteration.get(curProcessor)))));
        }

        while (counter != processors.size()) {
            buildQueue();
            opa = true;
        }

        threadPoolExecutor.shutdown();
        threadPoolExecutor.awaitTermination(10000, TimeUnit.MINUTES);

        for (Processor<T> processor : processors) {
            String curProcessor = processor.getId();

            while (results.get(curProcessor).size() > nullIteration) {
                results.get(curProcessor).remove(results.get(curProcessor).size() - 1);
            }
        }

        return results;
    }

    /**

     *
     *

     * @throws ExecutionException if the computation threw an exception

     * @throws InterruptedException if the current thread was interrupted while waiting

     */

    private void buildQueue() throws ExecutionException, InterruptedException {
        Set<String> delete = new HashSet<>();
        for (String curProcessor: gline) {
            Future<T> curTask = futureTasks.get(curProcessor);

            if (curTask.isDone()) {
                T result = curTask.get();
                if (result == null) {
                    nullIteration = Math.min(nullIteration, curIteration.get(curProcessor));
                }

                results.get(curProcessor).add(result);
                curTask.cancel(true);
                futureTasks.remove(curProcessor);
                curIteration.replace(curProcessor, curIteration.get(curProcessor) + 1);
                if (curIteration.get(curProcessor) == maxIterations) {
                    counter++;
                }
                delete.add(curProcessor);
            }
        }

        if (delete.isEmpty() && opa) return;
        for (String curProcessor : delete) gline.remove(curProcessor);

        for (String curProcessor : delete) {
            List<String> dependencies = graphOfDependencies.get(curProcessor);
            dependencies.add(curProcessor);
            for (String edge : dependencies) {
                if (curIteration.get(edge) < maxIterations && !gline.contains(edge) && checkDependencies(edge, curIteration.get(edge))) {
                    gline.add(edge);
                    futureTasks.put(edge, threadPoolExecutor.submit((Callable<T>) new ProcessorThread<T>(
                            idToProcessor.get(edge), makeListOfDependencies(edge, curIteration.get(edge)))));
                }
            }
        }
    }

    /**

     * @return a list of the results of processors on the iteration which processor is dependent

     */
    List<T> makeListOfDependencies(String processor, int iteration) {
        List<T> resultsDependencies = new ArrayList<>();
        List<String> dependencies = idToProcessor.get(processor).getInputIds();

        for (String edge : dependencies) {
            resultsDependencies.add(results.get(edge).get(iteration));
        }
        return resultsDependencies;
    }

    /**

     * @return true if all processors if all the processors on which we depend were executed and all previous

     *         iterations of current processor were executed

    */
    boolean checkDependencies(String processor, int iteration) {
        List<String> dependencies = idToProcessor.get(processor).getInputIds();

        for (String curProcessor : dependencies) {
            if (results.get(curProcessor).size() < iteration + 1) {
                return false;
            }
        }
        return true;
    }

    /**

     * create dependencies graph via dfs in format: if A depends from B, graph has  edge B -> A

     * @throws ProcessorException if dependencies loop detected

    */
    private void makeDependenciesGraph() throws ProcessorException {
        // initialize empty graph
        for (Processor<T> processor : processors) {
            graphOfDependencies.put(processor.getId(), new ArrayList<>());
        }

        for (Processor<T> processor : processors) {
            if (!used.contains(processor.getId())) {
                dfsGraphBuilder(processor.getId());
            }
        }
    }

    /**

     * @param processor --- current processor in question

     * @throws ProcessorException if dependencies loop detected

     */
    private void dfsGraphBuilder(String processor) throws ProcessorException {
        // source edges, needs to be reversed for our format of graph
        List<String> edges = idToProcessor.get(processor).getInputIds();

        used.add(processor);
        paintedVertexes.add(processor);

        if (edges.size() == 0) {
            // creators of information --- independent processors
            sourceProcessors.add(processor);
        }

        for (String edge : edges) {
            // use typical algorithm for searching loop(paint vertex when come and repaint when go out)
            if (paintedVertexes.contains(edge)) {
                throw new ProcessorException("Dependencies loop detected");
            }

            graphOfDependencies.get(edge).add(processor);
            if (!used.contains(edge)) {
                dfsGraphBuilder(edge);
            }
        }
        paintedVertexes.remove(processor);
    }

    /**
     * fill map of <id, processor>
     */
    void getIdProcessorMap() {
        for (Processor<T> processor : processors) {
            idToProcessor.put(processor.getId(), processor);
        }
    }
}


//package base;
//
//import java.util.*;
//import java.util.concurrent.*;
//
//public class MultiThreadSolution<T> implements Runner<T> {
//    private Set<Processor<T>> processors;
//    private int maxIterations;
//    private int maxThreads;
//    public long counter = 0;
//    public int nullIteration;
//
//    private Map<String, List<T>> results = new HashMap<>();
//    private Map<String, Processor<T>> idToProcessor = new HashMap<>();
//    private Map<String, ArrayList<String>> graphOfDependencies = new HashMap<>();
//    private HashMap<String, Integer> curIteration = new HashMap<>();
//    //private BlockingQueue<Runnable> queue;
//    ExecutorService threadPoolExecutor;
//
//    @Override
//    public Map<String, List<T>> runProcessors(Set<Processor<T>> processors, int maxThreads, int maxIterations)
//            throws ProcessorException, InterruptedException {
//        this.maxThreads = maxThreads;
//        this.nullIteration = maxIterations;
//        this.processors = processors;
//        this.maxIterations = maxIterations;
//        //queue = new ArrayBlockingQueue<>(maxThreads);
//
//        for (Processor<T> curProcessor : processors) {
//            curIteration.put(curProcessor.getId(), 0);
//            results.put(curProcessor.getId(), new LinkedList<>());
//        }
//
//        getIdProcessorMap();
//        buildDependenciesGraph();
//
//        threadPoolExecutor = Executors.newFixedThreadPool(maxThreads);
//
//
//        while (counter != processors.size()) {
//            threadPoolExecutor = Executors.newFixedThreadPool(maxThreads);
//            buildQueue();
//            threadPoolExecutor.shutdown();
//            threadPoolExecutor.awaitTermination(1000000000L, TimeUnit.MINUTES);
//        }
//
////        threadPoolExecutor.shutdown();
////        threadPoolExecutor.awaitTermination(1000000000L, TimeUnit.MINUTES);
//
//        for (Processor<T> processor : processors) {
//            String curProcessor = processor.getId();
//
//            while (results.get(curProcessor).size() > nullIteration) {
//                results.get(curProcessor).remove(results.get(curProcessor).size() - 1);
//            }
//        }
//
//        return results;
//    }
//
//    private void buildQueue() {
//        used = new HashSet<>();
//
//        for (String curProcessor : sourceProcessors) {
//            dfsBuildQueue(curProcessor);
//        }
//    }
//
//    private void dfsBuildQueue(String curProcessor) {
//        used.add(curProcessor);
//
//        if (curIteration.get(curProcessor) < maxIterations && checkDependencies(curProcessor, curIteration.get(curProcessor))) {
//
//            threadPoolExecutor.execute(new ProcessorThread<T>(this, idToProcessor.get(curProcessor), curIteration.get(curProcessor),
//                    buildListOfDependencies(curProcessor, curIteration.get(curProcessor))));
//
//
//            curIteration.replace(curProcessor, curIteration.get(curProcessor) + 1);
//            if (curIteration.get(curProcessor) == maxIterations) counter++;
//        }
//
//        for (int edge = 0; edge < graphOfDependencies.get(curProcessor).size(); edge++) {
//            String to = graphOfDependencies.get(curProcessor).get(edge);
//            if (!used.contains(to)) {
//                dfsBuildQueue(to);
//            }
//        }
//    }
//
//
//    List<T> buildListOfDependencies(String curProcessor, int iteration) {
//        List<T> resultsDependencies = new ArrayList<>();
//        List<String> dependencies = idToProcessor.get(curProcessor).getInputIds();
//
//        for (String edge : dependencies) {
//            resultsDependencies.add(results.get(edge).get(iteration));
//        }
//        return resultsDependencies;
//    }
//
//    boolean checkDependencies(String processor, int iteration) {
//        List<String> dependencies = idToProcessor.get(processor).getInputIds();
//
//        if (results.get(processor).size() < iteration) {
//            return false;
//        }
//        for (String curProcessor : dependencies) {
//            if (results.get(curProcessor).size() <= iteration) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public void setResults(String processor, T result) {
//        results.get(processor).add(result);
//    }
//
//    private HashSet<String> used = new HashSet<>();
//    private HashSet<String> paintedVertexes = new HashSet<>();
//
//    // create dependencies graph via dfs in format: if A depends from B, graph has  edge B -> A
//    private void buildDependenciesGraph() throws ProcessorException {
//        // initialize empty graph
//        for (Processor<T> processor : processors) {
//            graphOfDependencies.put(processor.getId(), new ArrayList<>());
//        }
//
//        for (Processor<T> processor : processors) {
//            if (!used.contains(processor.getId())) {
//                dfsBuildGraph(processor.getId());
//            }
//        }
//    }
//
//    // creators of information --- independent processors
//    private ArrayList<String> sourceProcessors = new ArrayList<>();
//
//    private void dfsBuildGraph(String curProcessor) throws ProcessorException {
//        // source edges, needs to be reversed for our format of graph
//        List<String> edges = idToProcessor.get(curProcessor).getInputIds();
//
//        used.add(curProcessor);
//        paintedVertexes.add(curProcessor);
//
//        if (edges.size() == 0) {
//            sourceProcessors.add(curProcessor);
//        }
//
//        for (String edge : edges) {
//            // use typical algorithm for searching loop(paint vertex when come and repaint when go out)
//            if (paintedVertexes.contains(edge)) {
//                throw new ProcessorException("Dependencies loop detected");
//            }
//
//            graphOfDependencies.get(edge).add(curProcessor);
//            if (!used.contains(edge)) {
//                dfsBuildGraph(edge);
//            }
//        }
//        paintedVertexes.remove(curProcessor);
//    }
//
//    // fill map of <id, processor>
//    void getIdProcessorMap() {
//        for (Processor<T> processor : processors) {
//            idToProcessor.put(processor.getId(), processor);
//        }
//    }
//}