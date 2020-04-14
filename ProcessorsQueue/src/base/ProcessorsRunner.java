package base;

import java.util.*;
import java.util.concurrent.*;

public class ProcessorsRunner<T> implements Runner<T> {
    private Set<Processor<T>> processors;
    private int maxIterations;
    private int maxThreads;
    public long counter = 0;
    public int nullIteration;

    private Map<String, List<T>> results = new HashMap<>();
    private Map<String, Processor<T>> idToProcessor = new HashMap<>();
    private Map<String, ArrayList<String>> graphOfDependencies = new HashMap<>();
    private HashMap<String, Integer> curIteration = new HashMap<>();
    //private BlockingQueue<Runnable> queue;
    ExecutorService threadPoolExecutor;

    @Override
    public Map<String, List<T>> runProcessors(Set<Processor<T>> processors, int maxThreads, int maxIterations)
            throws ProcessorException, InterruptedException {
        this.maxThreads = maxThreads;
        this.nullIteration = maxIterations;
        this.processors = processors;
        this.maxIterations = maxIterations;
        //queue = new ArrayBlockingQueue<>(maxThreads);

        for (Processor<T> curProcessor : processors) {
            curIteration.put(curProcessor.getId(), 0);
            results.put(curProcessor.getId(), new LinkedList<>());
        }

        getIdProcessorMap();
        buildDependenciesGraph();

        threadPoolExecutor = Executors.newFixedThreadPool(maxThreads);


        while (counter != processors.size()) {
            threadPoolExecutor = Executors.newFixedThreadPool(maxThreads);
            buildQueue();
            threadPoolExecutor.shutdown();
            threadPoolExecutor.awaitTermination(1000000000L, TimeUnit.MINUTES);
        }

//        threadPoolExecutor.shutdown();
//        threadPoolExecutor.awaitTermination(1000000000L, TimeUnit.MINUTES);

        for (Processor<T> processor : processors) {
            String curProcessor = processor.getId();

            while (results.get(curProcessor).size() > nullIteration) {
                results.get(curProcessor).remove(results.get(curProcessor).size() - 1);
            }
        }

        return results;
    }

    private void buildQueue() {
        used = new HashSet<>();

        for (String curProcessor : sourceProcessors) {
            dfsBuildQueue(curProcessor);
        }
    }

    private void dfsBuildQueue(String curProcessor) {
        used.add(curProcessor);

        if (curIteration.get(curProcessor) < maxIterations && checkDependencies(curProcessor, curIteration.get(curProcessor))) {

            threadPoolExecutor.execute(new ProcessorThread<T>(this, idToProcessor.get(curProcessor), curIteration.get(curProcessor),
                    buildListOfDependencies(curProcessor, curIteration.get(curProcessor))));

            curIteration.replace(curProcessor, curIteration.get(curProcessor) + 1);
            if (curIteration.get(curProcessor) == maxIterations) counter++;
        }

        for (int edge = 0; edge < graphOfDependencies.get(curProcessor).size(); edge++) {
            String to = graphOfDependencies.get(curProcessor).get(edge);
            if (!used.contains(to)) {
                dfsBuildQueue(to);
            }
        }
    }


    List<T> buildListOfDependencies(String curProcessor, int iteration) {
        List<T> resultsDependencies = new ArrayList<>();
        List<String> dependencies = idToProcessor.get(curProcessor).getInputIds();

        for (String edge : dependencies) {
            resultsDependencies.add(results.get(edge).get(iteration));
        }
        return resultsDependencies;
    }

    boolean checkDependencies(String processor, int iteration) {
        List<String> dependencies = idToProcessor.get(processor).getInputIds();

        if (results.get(processor).size() < iteration) {
            return false;
        }
        for (String curProcessor : dependencies) {
            if (results.get(curProcessor).size() <= iteration) {
                return false;
            }
        }
        return true;
    }

    public void setResults(String processor, T result) {
        results.get(processor).add(result);
    }

    private HashSet<String> used = new HashSet<>();
    private HashSet<String> paintedVertexes = new HashSet<>();

    // create dependencies graph via dfs in format: if A depends from B, graph has  edge B -> A
    private void buildDependenciesGraph() throws ProcessorException {
        // initialize empty graph
        for (Processor<T> processor : processors) {
            graphOfDependencies.put(processor.getId(), new ArrayList<>());
        }

        for (Processor<T> processor : processors) {
            if (!used.contains(processor.getId())) {
                dfsBuildGraph(processor.getId());
            }
        }
    }

    // creators of information --- independent processors
    private ArrayList<String> sourceProcessors = new ArrayList<>();

    private void dfsBuildGraph(String curProcessor) throws ProcessorException {
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
                dfsBuildGraph(edge);
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
