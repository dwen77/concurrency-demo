package com.example.concurrent.completablefuture;

import com.example.concurrent.TaskList;
import com.example.concurrent.helper.ThrowingFunction;
import com.example.concurrent.helper.ThrowingSupplier;
import com.example.concurrent.httpclient.ApacheHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public class BlockingApacheHttpClient {
    
    public static void main(String[] args) throws Exception {
        log.info("CPU Core: " + Runtime.getRuntime().availableProcessors());
        log.info("CommonPool Parallelism: " + ForkJoinPool.commonPool().getParallelism());
        log.info("CommonPool Common Parallelism: " + ForkJoinPool.getCommonPoolParallelism());
        run();
    }

    private static void run() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ApacheHttpClient apacheHttpClient = new ApacheHttpClient(httpClient);
        CompletableFuture<Void> all = CompletableFuture.allOf(sendRequests(apacheHttpClient))
                .exceptionally(ThrowingFunction.unchecked(throwable -> {
                    httpClient.close();
                    throw throwable;
                }));
        all.join();
        stopWatch.stop();
        httpClient.close();
        log.info("execution took {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
    }

    private static CompletableFuture<?>[] sendRequests(ApacheHttpClient apacheHttpClient) {
        return TaskList.tasks
                .mapToObj(taskId -> CompletableFuture.supplyAsync(sendRequest(apacheHttpClient, taskId)))
                .toArray(CompletableFuture[]::new);
    }

    private static Supplier<String> sendRequest(ApacheHttpClient apacheHttpClient, Integer taskId) {
        log.info("run async task {}", taskId);
        return ThrowingSupplier.unchecked(() -> apacheHttpClient.syncSend(taskId));
    }
}
