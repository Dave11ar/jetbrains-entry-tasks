package base;

import java.util.*;
import java.util.concurrent.*;

public class ProcessorsRunner<T> implements Runner<T> {
    private Set<Processor<T>> processors;
    private int maxThreads;
    private int matIterations;
    public int nullIteration;
    private HashMap<String, List<T>> results;

    private HashMap<String, Processor<T>> idToProcessor = new HashMap<>();
    
    @Override
    public Map<String, List<T>> runProcessors(Set<Processor<T>> processors, int maxThreads, int maxIterations) throws ProcessorException, InterruptedException {
        this.nullIteration = maxIterations;
        this.processors = processors;
        this.maxThreads = maxThreads;
        this.matIterations = maxIterations;
        this.results = new HashMap<>();

        for (Processor<T> curProcessor : processors) {
            curIteration.put(curProcessor.getId(), 0);
            results.put(curProcessor.getId(), new LinkedList<>());
        }

        getIdProcessorMap();
        makeDependenciesGraph();
        bfs();

        //имеем граф зависимостей и очередь выполнения

        while (true) {
            BlockingQueue<Runnable> queue = buildQueue();
            if (queue.isEmpty()) {
                break;
            }

            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

            for (Runnable run : queue) {
                threadPoolExecutor.execute(run);
            }
            threadPoolExecutor.shutdown();

            threadPoolExecutor.awaitTermination(1, TimeUnit.MINUTES);
        }

        for (Processor<T> processor : processors) {
            String curProcessor = processor.getId();

            while (results.get(curProcessor).size() > nullIteration) {
                results.get(curProcessor).remove(results.get(curProcessor).size() - 1);
            }
        }

        return results;
    }

    private HashMap<String, Integer> curIteration = new HashMap<>();

    BlockingQueue<Runnable> buildQueue() {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(maxThreads);

        for (String curProcessor : processorsQueue) {
            if (queue.size() == maxThreads) break;
            if (curIteration.get(curProcessor) == matIterations) continue;

            if (checkDependencies(curProcessor, curIteration.get(curProcessor))) {
                queue.add(new ProcessorThread<T>(this, idToProcessor.get(curProcessor), curIteration.get(curProcessor),
                        makeListOfDependencies(curProcessor, curIteration.get(curProcessor))));
                curIteration.put(curProcessor, curIteration.get(curProcessor) + 1);
            }
        }
        return queue;
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

    // API RESULTS
    public T getResult(int iteration, String processor) {
        return results.get(processor).get(iteration);
    }

    public void setResults(String processor, T result) {
        results.get(processor).add(result);
    }
    // API RESULTS

    /*
        составляем очередь в от самых независимых вершин к самым зависимым
        и каждый раз идем слева направо и набираем независимые процессы пока можем либо пока
        меньше maxTreads
    */
    private ArrayList<String> processorsQueue = new ArrayList<>();
    private void bfs() {
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
    /*
        ИМЕЕМ ГРАФ ЗАВИСИМОСТЕЙ
        НАДО ПОСТРОИТЬ ОЧЕРЕДЬ ДЛЯ ТРЕДОВ



     */
    
    
    // SOME DEPENDENCIES GRAPH FLEX
    // creating graph
    private HashSet<String> used = new HashSet<>();
    private HashSet<String> curIterationUsed = new HashSet<>();
    private HashMap<String, ArrayList<String>> graphOfDependencies = new HashMap<>();
            
    private void makeDependenciesGraph() throws ProcessorException {
        for (Processor<T> processor : processors) graphOfDependencies.put(processor.getId(), new ArrayList<>());

        for (Processor<T> processor : processors) {

            if (!used.contains(processor.getId())) {
                curIterationUsed.clear();
                dfs(processor.getId());
            }
        }
    }

    private ArrayList<String> sourceProcessors = new ArrayList<>();
    
    private void dfs(String curProcessor) throws ProcessorException {
        List<String> edges = idToProcessor.get(curProcessor).getInputIds();
        used.add(idToProcessor.get(curProcessor).getId());
        curIterationUsed.add(idToProcessor.get(curProcessor).getId());

        if (edges.size() == 0) {
            sourceProcessors.add(curProcessor);
        }

        for (String edge : edges) {
            if (curIterationUsed.contains(edge)) {
                throw new ProcessorException("Dependencies cycle exception");
            }

            graphOfDependencies.get(edge).add(curProcessor);
            if (!used.contains(edge)) {
                dfs(edge);
            }
        }
    }
    
    void getIdProcessorMap() {
        for (Processor<T> processor : processors) {
            idToProcessor.put(processor.getId(), processor);
        }
    }
}
