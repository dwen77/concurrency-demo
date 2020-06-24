package com.example.concurrent.helper;

@FunctionalInterface
public interface ThrowingRunnable<E extends Throwable> {

    void run() throws E;

    static <E extends Throwable> Runnable unchecked(ThrowingRunnable<E> runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}
