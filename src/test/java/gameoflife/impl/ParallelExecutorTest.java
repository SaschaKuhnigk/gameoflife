package gameoflife.impl;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ParallelExecutorTest {

    @Test
    public void test() {
        ParallelExecutor parallelExecutor = new ParallelExecutor();
        final AtomicInteger ai = new AtomicInteger(0);
        Runnable r = new Runnable() { @Override public void run() {
            sleep(1);
            ai.incrementAndGet();
        }};
        for (int n = 1; n <= 100; ++n) {
            System.out.println(n);
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 100; ++i) {
                parallelExecutor.addTask(r);
            }
            assertThat(System.currentTimeMillis() - startTime, is(lessThan(5L)));
            parallelExecutor.finishAllAddedTasks();
            assertThat(ai.get(), is(n * 100));
            assertThat(System.currentTimeMillis() - startTime, is(lessThan(80L)));
        }
    }
                                                                               
    private static void sleep(long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Got interrupted.", e);
        }
    }

}
