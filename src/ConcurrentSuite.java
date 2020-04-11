import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Runner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.RunnerScheduler;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public final class ConcurrentSuite extends ClasspathSuite {

    public static Runner MulThread(Runner runner) {
        CopyOnWriteArrayList<Future<?>> futures = new CopyOnWriteArrayList<>();
        ConcurrentHashMap<Future<?>, Integer> reTryTimes = new ConcurrentHashMap<>();
        ConcurrentHashMap<Future<?>, Runnable> map = new ConcurrentHashMap<>();
        if (runner instanceof ParentRunner) {
            // setScheduler(RunnerScheduler scheduler):Sets a scheduler that
            // determines the order and parallelization of children
            // RunnerScheduler:Represents a strategy for scheduling when
            // individual test methods should be run (in serial or parallel)
            ((ParentRunner) runner).setScheduler(new RunnerScheduler() {
                private final ExecutorService fService = Executors
                        .newCachedThreadPool();

                @Override
                // Schedule a child statement to run
                public void schedule(Runnable childStatement) {
                    Future<?> future = fService.submit(childStatement);//如何获取每个方法的运行结果？
                    futures.add(future);
                    reTryTimes.put(future, 2);
                    map.put(future, childStatement);
                }

                @Override
                // Override to implement any behavior that must occur after all
                // children have been scheduled
                public void finished() {
                    while (futures.size() > 0) {
                        for (Future<?> f : futures) {
                            try {
                                f.get();
                                futures.remove(f);
                            } catch (Exception e) {
                                if (reTryTimes.get(f) > 0) {
                                    f = fService.submit(map.get(f));
                                    reTryTimes.put(f, reTryTimes.get(f) - 1);
                                }
                            }
                        }


                    }

                    this.fService.shutdown();
                }


            });
        }
        return runner;
    }

    public ConcurrentSuite(final Class<?> klass) throws InitializationError {
        // 调用父类ClasspathSuite构造函数
        // AllDefaultPossibilitiesBuilder根据不同的测试类定义（@RunWith的信息）返回Runner,使用职责链模式
        super(klass, new AllDefaultPossibilitiesBuilder(true) {
            @Override
            public Runner runnerForClass(Class<?> testClass) throws Throwable {
                List<RunnerBuilder> builders = Arrays.asList(new RunnerBuilder[]{ignoredBuilder(),
                        annotatedBuilder(), suiteMethodBuilder(),
                        junit3Builder(), junit4Builder()});
                for (RunnerBuilder each : builders) {
                    // 根据不同的测试类定义（@RunWith的信息）返回Runner
                    Runner runner = each.safeRunnerForClass(testClass);
                    if (runner != null)
                        // 方法级别，多线程执行
                        return MulThread(runner);
                }
                return null;
            }
        });

        // 类级别，多线程执行
        setScheduler(new RunnerScheduler() {
            private final ExecutorService fService = Executors
                    .newCachedThreadPool();

            @Override
            public void schedule(Runnable paramRunnable) {
                // TODO Auto-generated method stub
                fService.submit(paramRunnable);
            }

            @Override
            public void finished() {
                // TODO Auto-generated method stub
                try {
                    fService.shutdown();
                    fService.awaitTermination(Long.MAX_VALUE,
                            TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
            }

        });
    }

}
