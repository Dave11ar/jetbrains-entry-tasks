package base.tests;

import base.Processor;
import base.ProcessorException;

import java.util.List;

public class TestProcessorExecutor<T extends Long> implements Processor<T> {
    private List<String> parameters;
    private String id;
    private long startValue;

    public TestProcessorExecutor(List<String> parameters, String id, long startValue) {
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

    /**
     * emulation of processor work as Long calculations
     */
    @Override
    public T process(List<T> input) {
        long cur = startValue;

        for (T current : input) {
            cur = cur + Long.valueOf(current) * startValue;
        }

        startValue += cur;

        if (cur > 1000000000L)  {
            return null;
        }
        return (T) Long.valueOf(cur);
    }
}
