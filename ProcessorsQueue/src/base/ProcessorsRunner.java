package base;

import java.util.*;
import java.util.concurrent.*;

public class ProcessorsRunner<T> implements Runner<T> {
    private Set<Processor<T>> processors;
    private int maxThreads;
    public int maxIterations;
    public int nullIteration;
    private Map<String, List<T>> results;
    private Map<String, Processor<T>> idToProcessor = new HashMap<>();
    private Map<String, ArrayList<String>> graphOfDependencies = new HashMap<>();
    public  HashMap<String,  Integer> curIteration = new HashMap<>();
    private ExecutorService threadPoolExecutor;
    public int counter = 0;
    public Set<String> gline = new HashSet<>();
    private Map<String, Future<T>>futureTasks = new HashMap<>();

    @Override
    public Map<String, List<T>> runProcessors(Set<Processor<T>> processors, int maxThreads, int maxIterations)
            throws ProcessorException, InterruptedException, ExecutionException {
        this.nullIteration = maxIterations;
        this.processors = processors;
        this.maxThreads = maxThreads;
        this.maxIterations = maxIterations;
        this.results = new HashMap<>();

        for (Processor<T> curProcessor : processors) {
            curIteration.put(curProcessor.getId(), 0);
            results.put(curProcessor.getId(), new LinkedList<>());
        }

        getIdProcessorMap();
        makeDependenciesGraph();
        buildProcessorsQueue();

        threadPoolExecutor = new ThreadPoolExecutor(maxThreads, maxThreads, 10000L, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

        while (counter != processors.size()) {
            buildQueue();
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

    private void buildQueue() throws ExecutionException, InterruptedException {
        Set<String> delete = new HashSet<>();
        for (String curProcessor: gline) {
            Future<T> curTask = futureTasks.get(curProcessor);

            if (curTask.isDone()) {
                setResults(curProcessor, curTask.get());
                curTask.cancel(true);
                curIteration.replace(curProcessor, curIteration.get(curProcessor) + 1);
                if (curIteration.get(curProcessor) == maxIterations) counter++;
                delete.add(curProcessor);
            }
        }

        for (String curProcessor : delete) gline.remove(curProcessor);

        for (String curProcessor : processorsQueue) {
            if (curIteration.get(curProcessor) == maxIterations) continue;

            if (!gline.contains(curProcessor) && checkDependencies(curProcessor, curIteration.get(curProcessor))) {
                gline.add(curProcessor);
                futureTasks.put(curProcessor, threadPoolExecutor.submit((Callable<T>) new ProcessorThread<T>(idToProcessor.get(curProcessor),
                        makeListOfDependencies(curProcessor, curIteration.get(curProcessor)))));
            }
        }
    }

    List<T> makeListOfDependencies(String curProcessor, int iteration) {
        List<T> resultsDependencies = new ArrayList<>();
        List<String> dependencies = idToProcessor.get(curProcessor).getInputIds();

        for (String edge : dependencies) {
            resultsDependencies.add(results.get(edge).get(iteration));
        }
        return resultsDependencies;
    }

    boolean checkDependencies(String processor, int iteration) {
        List<String> dependencies = idToProcessor.get(processor).getInputIds();

        for (String curProcessor : dependencies) {
            if (results.get(curProcessor).size() < iteration + 1) {
                return false;
            }
        }
        return true;
    }

    public void setResults(String processor, T result) {
        synchronized (gline) {
            results.get(processor).add(result);
        }
    }

    private ArrayList<String> processorsQueue = new ArrayList<>();

    /*
        build queue of processors wih invariant:
        processor[i] can always be launched before processor[j] if i < j,
        and can't be launched before some(can be 0) of processor[k](), k < i
    */
    private void buildProcessorsQueue() {
        // using bfs in out graph of dependencies
        LinkedList<String> queue = new LinkedList<>(sourceProcessors);
        used = new HashSet<>(sourceProcessors);

        while (!queue.isEmpty()) {
            String curProcessor = queue.pop();
            processorsQueue.add(curProcessor);
            for (int i = 0; i < graphOfDependencies.get(curProcessor).size(); i++) {
                String to = graphOfDependencies.get(curProcessor).get(i);
                if (!used.contains(to)) {
                    used.add(to);
                    queue.add(to);
                }
            }
        }
    }

    private HashSet<String> used = new HashSet<>();
    private HashSet<String> paintedVertexes = new HashSet<>();

    // create dependencies graph via dfs in format: if A depends from B, graph has  edge B -> A
    private void makeDependenciesGraph() throws ProcessorException {
        // initialize empty graph
        for (Processor<T> processor : processors) {
            graphOfDependencies.put(processor.getId(), new ArrayList<>());
        }

        for (Processor<T> processor : processors) {
            if (!used.contains(processor.getId())) {
                dfs(processor.getId());
            }
        }
    }

    // creators of information --- independent processors
    private ArrayList<String> sourceProcessors = new ArrayList<>();

    private void dfs(String curProcessor) throws ProcessorException {
        // source edges, needs to be reversed for our format of graph
        List<String> edges = idToProcessor.get(curProcessor).getInputIds();

        used.add(curProcessor);
        paintedVertexes.add(curProcessor);

        if (edges.size() == 0) {
            sourceProcessors.add(curProcessor);
        }

        for (String edge : edges) {
            // use typical algorithm for searching loop(paint vertex when come and repaint when go out)
            if (paintedVertexes.contains(edge)) {
                throw new ProcessorException("Dependencies loop detected");
            }

            graphOfDependencies.get(edge).add(curProcessor);
            if (!used.contains(edge)) {
                dfs(edge);
            }
        }
        paintedVertexes.remove(curProcessor);
    }

    // fill map of <id, processor>
    void getIdProcessorMap() {
        for (Processor<T> processor : processors) {
            idToProcessor.put(processor.getId(), processor);
        }
    }
}