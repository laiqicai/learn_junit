import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.concurrent.*;

public class MultiThreadedRunner extends BlockJUnit4ClassRunner {
    private static final int RETRY_COUNT = 2;
    private final ExecutorService fService = Executors.newCachedThreadPool();
    CopyOnWriteArrayList<Future<?>> futures = new CopyOnWriteArrayList<>();
    public MultiThreadedRunner(Class<?> klass) throws InitializationError {
        super (klass);
    }


    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
        Description description = describeChild(method);
        if (method.getAnnotation(Ignore.class) != null) {
            notifier.fireTestIgnored(description);
        } else {
            Future<?> future = fService.submit(new Test(methodBlock(method), description, notifier));
            futures.add(future);
        }
    }

    @Override
    protected Statement childrenInvoker(final RunNotifier notifier) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                MultiThreadedRunner.super.childrenInvoker(notifier).evaluate();
                while (futures.size() > 0){
                    for (Future<?> t : futures){
                        if (t.isDone()){
                            futures.remove(t);
                        }
                    }
                }
            }
        };
    }

    class Test implements Runnable {
        private final Statement statement;
        private final Description description;
        private final RunNotifier notifier;

        public Test (Statement statement, Description description, RunNotifier notifier) {
            this.statement =statement;
            this.description = description;
            this.notifier = notifier;
        }
        @Override
        public void run () {
            EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
            eachNotifier.fireTestStarted();
            try {
                statement.evaluate();
            } catch (AssumptionViolatedException e) {
                eachNotifier.addFailedAssumption(e);
            } catch (Throwable e) {
                System.out.println("Retry test: " + description.getDisplayName());
                retry(eachNotifier, statement, e, description);
            } finally {
                eachNotifier.fireTestFinished();
            }
        }
    }
    private void retry(EachTestNotifier notifier, Statement statement, Throwable currentThrowable, Description info) {
        int failedAttempts = 0;
        Throwable caughtThrowable = currentThrowable;
        while (RETRY_COUNT > failedAttempts) {
            try {
                System.out.println("Retry attempt " + (failedAttempts + 1) + " for " + info.getDisplayName());
                statement.evaluate();
                return;
            } catch (Throwable t) {
                failedAttempts++;
                caughtThrowable = t;
            }
        }
        notifier.addFailure(caughtThrowable);
    }

}