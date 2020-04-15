package base.single;

import base.Processor;
import base.ProcessorException;
import base.Runner;

import java.util.*;

public class SingleThreadSolution<T> implements Runner<T> {
    private Set<Processor<T>> processors;
    public int nullIteration;

    private Map<String, List<T>> results;
    private Map<String, Processor<T>> idToProcessor;
    private Set<String> used;
    private int curIteration = 0;

    private Set<String> painted = new HashSet<>();

    public Map<String, List<T>> runProcessors(Set<Processor<T>> processors, int maxThreads, int maxIterations) throws ProcessorException {
        this.nullIteration = maxIterations;
        this.processors = processors;

        results = new HashMap<>();
        used = new HashSet<>();

        for (Processor<T> curProcessor : processors) {
            results.put(curProcessor.getId(), new LinkedList<>());
        }
        getIdProcessorMap();

        for (int i = 0; i < nullIteration; i++) {
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

    /**
     * Launch processor if all dependencies satisfied or do run(dependencies) before it

     * @param processor --- current processor in question

     * @throws ProcessorException if dependence loop detectes
     */
    private void run(Processor<T> processor) throws ProcessorException {
        String processorId = processor.getId();
        List<String> dependencies = processor.getInputIds();

        used.add(processorId);
        painted.add(processorId);
        if (dependencies != null) {
            for (String dependence : dependencies) {
                if (painted.contains(dependence)) {
                    throw new ProcessorException("Dependence loop detected");
                }
                if (used.contains(dependence)) {
                    continue;
                }
                run(idToProcessor.get(dependence));
            }
        }
        painted.remove(processorId);

        T result = idToProcessor.get(processorId).process(buildListOfDependencies(processorId, curIteration));
        if (result == null) {
            nullIteration = Math.min(nullIteration, curIteration);
            return;
        }

        results.get(processorId).add(result);
    }

    List<T> buildListOfDependencies(String curProcessor, int iteration) {
        List<T> resultsDependencies = new ArrayList<>();
        List<String> dependencies = idToProcessor.get(curProcessor).getInputIds();

        if (dependencies != null) {
            for (String edge : dependencies) {
                resultsDependencies.add(results.get(edge).get(iteration));
            }
        }
        return resultsDependencies;
    }

    private void getIdProcessorMap() {
        idToProcessor = new HashMap<>();
        for (Processor<T> processor : processors) {
            idToProcessor.put(processor.getId(), processor);
        }
    }
}
