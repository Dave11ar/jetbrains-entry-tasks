package base;

import java.util.List;
import java.util.concurrent.Callable;

public class ProcessorThread<T> extends Thread implements Callable<T> {
    private Processor<T> processor;
    private List<T> dependencies;

    public ProcessorThread(Processor<T> processor, List<T> dependencies) {
        this.processor = processor;
        this.dependencies = dependencies;
    }

    @Override
    public T call() throws Exception {
        try {
            return processor.process(dependencies);
        } catch (ProcessorException e) {
            throw new ProcessorException("Processor exception at processor: " + processor.getId());
        } finally {
            this.interrupt();
        }
    }
}

//package base;
//
//import java.util.List;
//
//public class ProcessorThread<T> extends Thread implements Runnable {
//    private MultiThreadSolution<T> mainClass;
//    private Processor<T> processor;
//    private int iteration;
//    private List<T> dependencies;
//
//    public ProcessorThread(MultiThreadSolution<T> mainClass, Processor<T> processor, int iteration, List<T> dependencies) {
//        this.mainClass = mainClass;
//        this.processor = processor;
//        this.iteration = iteration;
//        this.dependencies = dependencies;
//    }
//
//    @Override
//    public void run() {
//        try {
//            T result = processor.process(dependencies);
//            if (result == null) {
//                mainClass.nullIteration = Math.min(mainClass.nullIteration, iteration);
//                return;
//            }
//
//            mainClass.setResults(processor.getId(), result);
//        } catch (ProcessorException e) {
//            System.out.println("Processor exception at processor: " + processor.getId());
//        } finally {
//            this.interrupt();
//        }
//    }
//}
