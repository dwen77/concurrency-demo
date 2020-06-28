package com.example.concurrent.completablefuture;

import com.example.concurrent.HttpClientCollection;
import com.example.concurrent.TaskList;
import com.example.concurrent.helper.ThrowingFunction;
import com.example.concurrent.helper.ThrowingRunnable;
import com.example.concurrent.httpclient.JettyClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NonBlockingMultipleHttpClientWaitCompletionOnEach {

    public static void main(String[] args) throws Exception {
        log.info("CPU Core: " + Runtime.getRuntime().availableProcessors());
        log.info("CommonPool Parallelism: " + ForkJoinPool.commonPool().getParallelism());
        log.info("CommonPool Common Parallelism: " + ForkJoinPool.getCommonPoolParallelism());
        run();
    }

    private static void run() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        CompletableFuture<Void> all = CompletableFuture.allOf(
                TaskList.taskStreamSupplier.get()
                        .mapToObj(taskId -> CompletableFuture.runAsync(sendRequest(new JettyClient(HttpClientCollection.getHttpClient(taskId)), taskId)))
                        .toArray(CompletableFuture[]::new))
                .exceptionally(ThrowingFunction.unchecked(throwable -> {
                    throw throwable;
                }));
        all.join();
        stopWatch.stop();
        TaskList.taskStreamSupplier.get().forEach(value -> {
            try {
                HttpClientCollection.getHttpClient(value).stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        log.info("execution took {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
    }

    private static Runnable sendRequest(JettyClient jettyClient, Integer taskId) {
        return ThrowingRunnable.unchecked(
                () -> {
                    log.info("callbackSend {}" , taskId);
                    CompletableFuture<String> completableFuture = jettyClient.callbackSend(taskId);
                    log.info("wait {}" , taskId);
                    completableFuture.get();
                }
        );
    }
}
