import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadedRunner extends BlockJUnit4ClassRunner {

    private AtomicInteger TaskNums;
    private final ExecutorService pool = Executors.newFixedThreadPool(3);

    public MultiThreadedRunner (Class<?> klass) throws InitializationError {
        super (klass);
        TaskNums = new AtomicInteger(testCount());
    }


    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {//notifier对每个方法的吗？
        Future<?> future = pool.submit(new Test(method, notifier));
        try {
            future.get();
        }catch (Exception e){
            runChild(method,notifier);
        }
        if (future.isDone()){
            TaskNums.decrementAndGet();
        }
        if (TaskNums.equals(new AtomicInteger(0))){
            pool.shutdown();
        }

    }

    @Override
    protected Statement childrenInvoker(final RunNotifier notifier) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                MultiThreadedRunner.super.childrenInvoker(notifier).evaluate();
                while (TaskNums.get() > 0) {
                    Thread.sleep(1000);
                }

            }
        };
    }

    class Test implements Runnable {
        private final FrameworkMethod method;
        private final RunNotifier notifier;

        public Test (final FrameworkMethod method, final RunNotifier notifier) {
            this.method = method;
            this.notifier = notifier;
        }
        public void run () {
            MultiThreadedRunner.super.runChild(method, notifier);
        }
    }

}