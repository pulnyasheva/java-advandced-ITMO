package info.kgeorgiy.ja.pulnikova.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

/**
 * Implementation class for {@link ParallelMapper} interface.
 */
public class ParallelMapperImpl implements ParallelMapper {

    private final Queue<Runnable> queue;
    private final List<Thread> listThread;

    /**
     * Constructor based on the number of threads
     *
     * @param threads counts of treads
     */
    public ParallelMapperImpl(int threads) {
        listThread = new ArrayList<>();
        queue = new ArrayDeque<>();
        for (int i = 0; i < threads; i++) {
            listThread.add(new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        Runnable element;
                        synchronized (queue) {
                            while (queue.isEmpty()) {
                                queue.wait();
                            }
                            element = queue.poll();
                        }
                        element.run();
                    }
                } catch (InterruptedException e) {
                    // Поток заканчивает работу
                } finally {
                    Thread.currentThread().interrupt();
                }
            }));
            listThread.get(i).start();
        }
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performed in parallel.
     *
     * @param f    function to calculate
     * @param args elements to calculate
     * @param <T>  argument type to calculate
     * @param <R>  argument type result
     * @return {@link List} calculation results
     * @throws InterruptedException if calling thread was interrupted
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        Counter count = new Counter(args.size());
        List<R> answer = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            answer.add(null);
        }
        for (int i = 0; i < args.size(); i++) {
            synchronized (queue) {
                int finalI = i;
                queue.add(() -> {
                    addInQueue(finalI, args.get(finalI), f, answer, count);
                });
                queue.notify();
            }
        }
        count.waitFinish();
        return answer;
    }

    <T, R> void addInQueue(int finalI, T element,
                           Function<? super T, ? extends R> f,
                           List<R> answer,
                           Counter count) {
        try {
            answer.set(finalI,
                    f.apply(element));
        } catch (RuntimeException e) {
            count.addException(e);
        }
        count.increment();
    }

    @Override
    public void close() {
        boolean isInterrupt = false;
        for (Thread thread : listThread) {
            thread.interrupt();
        }
        for (int i = 0; i < listThread.size(); ) {
            try {
                listThread.get(i).join();
                i++;
            } catch (InterruptedException e) {
                isInterrupt = true;
            }
        }
        if (isInterrupt) {
            Thread.currentThread().interrupt();
        }
    }


    private static class Counter {
        private int count = 0;
        private final int size;
        private RuntimeException exception = null;

        Counter(int size) {
            this.size = size;
        }

        synchronized void increment() {
            count++;
            if (count == size) {
                this.notify();
            }
        }

        synchronized void waitFinish() throws InterruptedException {
            while (count < size) {
                this.wait();
            }
            if (exception != null) {
                throw exception;
            }
        }

        synchronized void addException(RuntimeException exception) {
            if (this.exception == null) {
                this.exception = exception;
            } else {
                this.exception.addSuppressed(exception);
            }
        }
    }

}
