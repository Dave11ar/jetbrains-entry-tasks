package base;

import java.util.List;

public class ProcessorThread<T> implements Runnable {
    private ProcessorsRunner<T> mainClass;
    private Processor<T> processor;
    private int iteration;
    private List<T> dependencies;

    public ProcessorThread(ProcessorsRunner<T> mainClass, Processor<T> processor, int iteration, List<T> dependencies) {
        this.mainClass = mainClass;
        this.processor = processor;
        this.iteration = iteration;
        this.dependencies = dependencies;
    }

    @Override
    public void run() {
        long jopa = 0;
        while (jopa < 5000000000L) jopa++;

        try {
            T result = processor.process(dependencies);
            if (result == null) {
                mainClass.nullIteration = Math.min(mainClass.nullIteration, iteration);
            }

            mainClass.setResults(processor.getId(), processor.process(dependencies));
        } catch (Exception e) {
            System.out.println("Processor exception at processor: " + processor.getId());
        }
    }
}
