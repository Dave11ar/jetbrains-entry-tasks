package base.tests;

import base.Processor;
import base.ProcessorException;
import base.Runner;

import java.util.*;

public class SingleThreadSolution<T> implements Runner<T> {
    private Set<Processor<T>> processors;
    private int maxThreads;
    private int maxIterations;
    public int nullIteration;

    private Map<String, List<T>> results;
    private Map<String, Processor<T>> idToProcessor = new HashMap<>();
    private Set<String> used = new HashSet<>();
    private int curIteration = 0;

    private Set<String> painted = new HashSet<>();

    public Map<String, List<T>> runProcessors(Set<Processor<T>> processors, int maxThreads, int maxIterations) throws ProcessorException {
        this.nullIteration = maxIterations;
        this.processors = processors;
        this.maxThreads = maxThreads;
        this.maxIterations = maxIterations;
        this.results = new HashMap<>();

        for (Processor<T> curProcessor : processors) {
            results.put(curProcessor.getId(), new LinkedList<>());
        }
        getIdProcessorMap();

        for (int i = 0; i < maxIterations; i++) {
            for (Processor<T> curProcessor : processors) {
                if (!used.contains(curProcessor.getId())) {
                    run(curProcessor);
                }
            }
            used.clear();
            curIteration++;
        }

        return results;
    }

    private void run(Processor<T> curProcessor) throws ProcessorException {
        String processorId = curProcessor.getId();
        List<String> dependencies = curProcessor.getInputIds();

        used.add(processorId);
        painted.add(processorId);
        for (String dependence : dependencies) {
            if (painted.contains(dependence)) {
                throw new ProcessorException("Dependence loop detected");
            }
            if (used.contains(dependence)) {
                continue;
            }
            run(idToProcessor.get(dependence));
        }
        painted.remove(processorId);
        results.get(processorId).add(idToProcessor.get(processorId).process(buildListOfDependencies(processorId, curIteration)));
    }

    List<T> buildListOfDependencies(String curProcessor, int iteration) {
        List<T> resultsDependencies = new ArrayList<>();
        List<String> dependencies = idToProcessor.get(curProcessor).getInputIds();

        for (String edge : dependencies) {
            resultsDependencies.add(results.get(edge).get(iteration));
        }
        return resultsDependencies;
    }

    private void getIdProcessorMap() {
        for (Processor<T> processor : processors) {
            idToProcessor.put(processor.getId(), processor);
        }
    }
}
