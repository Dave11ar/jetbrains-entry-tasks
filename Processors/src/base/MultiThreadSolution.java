package base;

import java.util.*;
import java.util.concurrent.*;

public class MultiThreadSolution<T> implements Runner<T> {
    private Set<Processor<T>> processors;

    private ExecutorService threadPoolExecutor;

    // map of <processorID, Future<T> in execution queue for threadPoolExecutor>
    private Map<String, Future<T>> futureTasks = new HashMap<>();

    // if A depends from B, graph has  edge B -> A
    private Map<String, List<String>> graphOfDependencies = new HashMap<>();

    private int nullIteration; // this iteration and next should be ignored in output
    private int completedProcessorIterations = 0; // number of processors which completed all maxIterations
    private Set<String> executingProcessors = new HashSet<>(); // processors which executing right now
    private List<String> sourceProcessors = new ArrayList<>(); // processors which don't depend from other
    private Map<String,  Integer> curIteration = new HashMap<>(); // current number of iteration for each processor
    private Map<String, Processor<T>> idToProcessor = new HashMap<>(); // map of <processorID, processor>
    private Map<String, List<T>> results = new HashMap<>();


    @Override
    public Map<String, List<T>> runProcessors(Set<Processor<T>> processors, int maxThreads, int maxIterations)
            throws ProcessorException, InterruptedException, ExecutionException {
        this.processors = processors;

        nullIteration = maxIterations;

        for (Processor<T> curProcessor : processors) {
            curIteration.put(curProcessor.getId(), 0);
            results.put(curProcessor.getId(), new LinkedList<>());
        }

        getIdProcessorMap();
        makeDependenciesGraph();

        threadPoolExecutor = new ThreadPoolExecutor(0, maxThreads,
                10L, TimeUnit.HOURS, new LinkedBlockingQueue<>());

        // first load of queue of executing
        for (String curProcessor : sourceProcessors) {
            executingProcessors.add(curProcessor);
            futureTasks.put(curProcessor, threadPoolExecutor.submit((Callable<T>)
                    new ProcessorThread<T>(idToProcessor.get(curProcessor), makeListOfDependencies(curProcessor,
                            curIteration.get(curProcessor)))));
        }

        while (completedProcessorIterations != processors.size()) {
            addNewTasks();
        }

        threadPoolExecutor.shutdown();
        threadPoolExecutor.awaitTermination(10L, TimeUnit.HOURS);

        for (Processor<T> processor : processors) {
            String curProcessor = processor.getId();

            // ignore iterations after null iteration
            while (results.get(curProcessor).size() > nullIteration) {
                results.get(curProcessor).remove(results.get(curProcessor).size() - 1);
            }
        }

        return results;
    }

    /**
     * Finishes executed processor tasks and write it to results, after if
     * there were executed tasks, tries to add new

     * @throws ExecutionException if the computation threw an exception

     * @throws InterruptedException if the current thread was interrupted while waiting

     * @throws ProcessorException if getInputIds() contains unknown processor ID
     */
    private void addNewTasks() throws ExecutionException, InterruptedException, ProcessorException {
        Set<String> delete = new HashSet<>();
        for (String curProcessor: executingProcessors) {
            Future<T> curTask = futureTasks.get(curProcessor);

            if (curTask.isDone()) {
                T result = curTask.get();

                if (result == null) {
                    nullIteration = Math.min(nullIteration, curIteration.get(curProcessor));
                    for (Processor<T> processor : processors) {
                        if (curIteration.get(processor.getId()) >= nullIteration)  {
                            completedProcessorIterations++;
                        }
                    }
                }

                results.get(curProcessor).add(result);
                futureTasks.remove(curProcessor);
                curIteration.replace(curProcessor, curIteration.get(curProcessor) + 1);

                if (curIteration.get(curProcessor) >= nullIteration) {
                    completedProcessorIterations++;
                }
                delete.add(curProcessor);
            }
        }

        // If no task is completed we cannot add a new one
        if (delete.isEmpty()) return;

        for (String curProcessor : delete) executingProcessors.remove(curProcessor);

        for (String curProcessor : delete) {
            // These processors can be available to start new iteration
            List<String> needToCheck = graphOfDependencies.get(curProcessor);
            needToCheck.add(curProcessor);

            for (String edge : needToCheck) {
                if (curIteration.get(edge) < nullIteration && !executingProcessors.contains(edge) &&
                        checkDependencies(edge, curIteration.get(edge))) {

                    executingProcessors.add(edge);
                    futureTasks.put(edge, threadPoolExecutor.submit((Callable<T>) new ProcessorThread<T>(
                            idToProcessor.get(edge), makeListOfDependencies(edge, curIteration.get(edge)))));
                }
            }
        }
    }

    /**
     * @return a list of the results of processors on the iteration which processor is dependent

     * @throws ProcessorException if getInputIds() contains unknown processor ID
     */
    List<T> makeListOfDependencies(String processor, int iteration) throws ProcessorException {
        List<T> resultsDependencies = new ArrayList<>();
        List<String> dependencies = idToProcessor.get(processor).getInputIds();

        if (dependencies != null) {
            for (String edge : dependencies) {
                if (!results.containsKey(edge)) {
                    throw new ProcessorException("Unknown processor ID: " + processor);
                }
                resultsDependencies.add(results.get(edge).get(iteration));
            }
        }
        return resultsDependencies;
    }

    /**
     * @return true if all processors if all the processors on which we depend were executed and all previous
     *         iterations of current processor were executed

     * @throws ProcessorException if getInputIds() contains unknown processor ID
    */
    boolean checkDependencies(String processor, int iteration) throws ProcessorException {
        List<String> dependencies = idToProcessor.get(processor).getInputIds();

        if (dependencies != null) {
            for (String edge : dependencies) {
                if (!results.containsKey(edge)) {
                    throw new ProcessorException("Unknown processor ID: " + processor);
                }
                if (results.get(edge).size() < iteration + 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private Set<String> used; // used array for dfs
    private Set<String> paintedVertexes = new HashSet<>(); // used array for searching loops
    /**
     * Creates dependencies graph via dfs in format: if A depends from B, graph has  edge B -> A

     * @throws ProcessorException if dependencies loop detected
    */
    private void makeDependenciesGraph() throws ProcessorException {
        // initialize empty graph
        used = new HashSet<>();
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

     * @throws ProcessorException if dependencies loop detected or if getInputIds() contains unknown processor ID
     */
    private void dfsGraphBuilder(String processor) throws ProcessorException {
        // source edges, needs to be reversed for our format of graph
        if (!idToProcessor.containsKey(processor)) {
            throw new ProcessorException("Unknown processor ID: " + processor);
        }
        List<String> edges = idToProcessor.get(processor).getInputIds();

        used.add(processor);
        paintedVertexes.add(processor);

        if (edges == null || edges.size() == 0) {
            // creators of information --- independent processors
            sourceProcessors.add(processor);
        } else {
            for (String edge : edges) {
                if (!results.containsKey(edge)) {
                    throw new ProcessorException("Unknown processor ID: " + processor);
                }

                // use typical algorithm for searching loop(paint vertex when come and repaint when go out)
                if (paintedVertexes.contains(edge)) {
                    throw new ProcessorException("Dependencies loop detected");
                }

                graphOfDependencies.get(edge).add(processor);
                if (!used.contains(edge)) {
                    dfsGraphBuilder(edge);
                }
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
