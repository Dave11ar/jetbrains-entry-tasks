package base.tests;

import base.Processor;
import base.ProcessorException;

import java.util.List;

public class ProcessorExecutor<T extends Long> implements Processor<T> {
    private List<String> parameters;
    private String id;
    private long startValue;

    public ProcessorExecutor(List<String> parameters, String id, long startValue) {
        this.parameters = parameters;
        this.id = id;
        this.startValue = startValue;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<String> getInputIds() {
        return parameters;
    }

    @Override
    public T process(List<T> input) throws ProcessorException {
        long cur = startValue;
        for (long i = 0; i < 2000000L; i++) i = i + i - i;
        for (T current : input) {
            cur = Math.abs(Long.min(1000000000L, cur + Long.valueOf(current) + startValue));
        }
        //startValue += Math.abs(Long.min(1000000000L, cur));
        startValue++;

        return (T) Long.valueOf(cur);
    }
}
