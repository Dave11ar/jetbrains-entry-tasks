package base.multi;

import base.Processor;
import base.ProcessorException;

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
