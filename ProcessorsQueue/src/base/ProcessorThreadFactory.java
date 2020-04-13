package base;

import java.util.concurrent.ThreadFactory;

public class ProcessorThreadFactory implements ThreadFactory {
    public Thread newThread(Runnable r) {
        return new Thread(r);
    }
}

