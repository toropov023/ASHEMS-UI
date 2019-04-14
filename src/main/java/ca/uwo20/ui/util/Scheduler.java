package ca.uwo20.ui.util;

import ca.uwo20.ui.Main;
import javafx.application.Platform;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Author: toropov
 * Date: 2/20/2019
 */
public class Scheduler extends Thread {
    private static final long TIME_STEP = 50;
    private static Scheduler i;

    private final Set<Task> tasks = ConcurrentHashMap.newKeySet();

    private Scheduler() {
        start();
    }

    private static Scheduler getI() {
        if (i == null) {
            i = new Scheduler();
        }
        return i;
    }

    /**
     * Create an asynchronous task to be executed as soon as possible
     *
     * @param runnable Runnable to be executed
     */
    public static void run(Runnable runnable) {
        run(runnable, 0, TimeUnit.SECONDS);
    }

    /**
     * Create an asynchronous task to be executed after the specified delay.
     * The precision of the delay if not guaranteed
     *
     * @param runnable Runnable to be executed
     * @param delay    Delay in TimeUnit(s) specified
     * @param timeUnit The TimeUnit of the delay
     */
    public static void run(Runnable runnable, long delay, TimeUnit timeUnit) {
        createThread((l) -> runnable.run(), delay, timeUnit, false);
    }

    /**
     * Create an asynchronous task to be executed repeatedly with the specified delay.
     * The precision of the delay if not guaranteed
     *
     * @param consumer The consumer to be called when the task executes.
     *                 {@link Task} provides the counter reference
     *                 and a method ({@link Task#terminate()}) to terminate the task
     * @param delay    Delay in TimeUnit(s) specified
     * @param timeUnit The TimeUnit of the delay
     */
    public static void runRepeated(Consumer<Task> consumer, long delay, TimeUnit timeUnit) {
        createThread(consumer, delay, timeUnit, true);
    }

    private static void createThread(Consumer<Task> consumer, long delay, TimeUnit timeUnit, boolean repeated) {
        long timeDelta = TimeUnit.MILLISECONDS.convert(delay, timeUnit) / TIME_STEP;
        long time = repeated ? 0 : timeDelta;

        getI().tasks.add(new Task(consumer, timeDelta, new AtomicLong(time), repeated));
    }

    /**
     * Runs the task on the main thread
     *
     * @param runnable The runnable to execute
     */
    public static void runOnMainThread(Runnable runnable) {
        Platform.runLater(runnable);
    }

    @Override
    public void run() {
        while (Main.isRunning) {
            for (Task task : tasks) {
                if (task.timeWhenToExecute.getAndDecrement() <= 0) {
                    task.runnable.accept(task);

                    if (!task.terminated && task.repeated) {
                        task.counter.incrementAndGet();
                        task.timeWhenToExecute.set(task.timeDelta);
                    } else {
                        task.terminate();
                    }
                }
            }
            tasks.removeIf(t -> t.terminated);

            try {
                sleep(TIME_STEP);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiredArgsConstructor
    public static class Task {
        private final Consumer<Task> runnable;
        private final long timeDelta;
        private final AtomicLong timeWhenToExecute;
        private final boolean repeated;
        @Getter
        private final AtomicLong counter = new AtomicLong();
        private boolean terminated;

        /**
         * Terminates the task so that it doesn't get executed the next time it is scheduled
         */
        public void terminate() {
            terminated = true;
        }
    }
}
