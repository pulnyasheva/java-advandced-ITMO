package info.kgeorgiy.ja.pulnikova.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class IterativeParallelism implements ScalarIP {

    private final ParallelMapper mapper;

    public IterativeParallelism() {
        this.mapper = null;
    }

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Returns maximum value.
     *
     * @param threads    number of concurrent threads.
     * @param values     values to get maximum of.
     * @param comparator value comparator.
     * @param <T>        value type.
     * @return maximum of given values
     * @throws InterruptedException             if executing thread was interrupted.
     * @throws java.util.NoSuchElementException if no values are given.
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return workWithThread(threads, values,
                value -> value.stream().max(comparator).orElse(null),
                value -> value.stream().max(comparator).orElse(null));
    }

    /**
     * Returns minimum value.
     *
     * @param threads    number of concurrent threads.
     * @param values     values to get minimum of.
     * @param comparator value comparator.
     * @param <T>        value type.
     * @return minimum of given values
     * @throws InterruptedException             if executing thread was interrupted.
     * @throws java.util.NoSuchElementException if no values are given.
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    /**
     * Returns whether all values satisfy predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @param <T>       value type.
     * @return whether all values satisfy predicate or {@code true}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return workWithThread(threads, values,
                value -> value.stream().allMatch(predicate),
                value -> value.stream().allMatch(element -> element));
    }

    /**
     * Returns whether any of values satisfies predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @param <T>       value type.
     * @return whether any value satisfies predicate or {@code false}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    /**
     * Returns number of values satisfying predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @param <T>       value type.
     * @return number of values satisfying predicate.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return workWithThread(threads, values,
                value -> value.stream().filter(predicate).toList().size(),
                value -> value.stream().mapToInt(Integer::intValue).reduce(0, Integer::sum));
    }


    /**
     * Processing with threads using the passed function
     *
     * @param threads    number of concurrent threads.
     * @param values     values to test.
     * @param func       function for working with elements
     * @param funcAnswer function for working with the results of the thread
     * @param <R>        type of return value
     * @param <T>        the type of value that we will work with
     * @return a value satisfying func
     * @throws InterruptedException if executing thread was interrupted.
     */
    private <R, T> R workWithThread(int threads, List<? extends T> values,
                                    Function<List<? extends T>, R> func,
                                    Function<List<? extends R>, R> funcAnswer) throws InterruptedException {
        threads = Math.min(threads, values.size());
        int lenListForThread = values.size() / threads;
        int additionList = values.size() % threads;
        List<R> listAnswer = new ArrayList<>();
        List<List<? extends T>> listSublists = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            listAnswer.add(null);
        }
        int right = 0;
        for (int i = 0; i < threads; i++) {
            int left = right;
            right += lenListForThread + (i < additionList ? 1 : 0);
            int finalRight = right;
            listSublists.add(values.subList(left, finalRight));
        }
        if (mapper == null) {
            List<R> finalListAnswer = listAnswer;
            List<Thread> listThread = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                int finalI = i;
                Thread thread = new Thread(() ->
                        finalListAnswer.set(finalI,
                                func.apply(listSublists.get(finalI))));
                thread.start();
                listThread.add(thread);
            }
            Throwable th = null;
            for (int i = 0; i < listThread.size(); ) {
                try {
                    listThread.get(i).join();
                    i++;
                } catch (InterruptedException e) {
                    if (th == null) {
                        for (int j = i; j < listThread.size(); j++) {
                            listThread.get(j).interrupt();
                        }
                    }
                    th = new InterruptedException();
                    th.addSuppressed(e);
                }
            }
            if (th != null) {
                throw new InterruptedException("Failed join threads" + th.getMessage());
            }
        } else {
            listAnswer = mapper.map(func, listSublists);
        }
        return funcAnswer.apply(listAnswer);
    }
}
