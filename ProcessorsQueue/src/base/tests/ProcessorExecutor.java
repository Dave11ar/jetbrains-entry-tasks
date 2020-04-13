package base.tests;

import base.Processor;
import base.ProcessorException;

import java.util.List;

public class ProcessorExecutor<T extends Integer> implements Processor<T> {
    private List<String> parameters;
    private String id;
    private int startValue;

    public ProcessorExecutor(List<String> parameters, String id, int startValue) {
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

        long jopa = 0;
        while (jopa < 500000L * Long.parseLong(id) * Long.parseLong(id) ) jopa++;

        Integer cur = startValue;

        for (T current : input) {
            cur = Integer.max(cur, Integer.valueOf(current));
        }

        return (T) Integer.valueOf(cur.intValue());
    }
}
