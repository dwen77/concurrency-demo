package com.example.concurrent.completablefuture;

import com.example.concurrent.TaskList;
import com.example.concurrent.helper.ThrowingFunction;
import com.example.concurrent.helper.ThrowingSupplier;
import com.example.concurrent.httpclient.JettyClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.util.concurrent.*;
import java.util.function.Supplier;

@Slf4j
public class CustomThreadPoolBlockingHttpClient {
    
    public static void main(String[] args) throws Exception {
        log.info("CPU Core: " + Runtime.getRuntime().availableProcessors());
        log.info("CommonPool Parallelism: " + ForkJoinPool.commonPool().getParallelism());
        log.info("CommonPool Common Parallelism: " + ForkJoinPool.getCommonPoolParallelism());
        run();
    }

    private static void run() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        HttpClient httpClient = new HttpClient(sslContextFactory);
        JettyClient jettyClient = new JettyClient(httpClient);
        httpClient.start();
        ExecutorService executor = Executors.newCachedThreadPool();
        CompletableFuture<Void> all = CompletableFuture.allOf(sendRequests(jettyClient, executor))
                .exceptionally(ThrowingFunction.unchecked(throwable -> {
                    httpClient.stop();
                    executor.shutdown();
                    throw throwable;
                }));
        all.join();
        stopWatch.stop();
        httpClient.stop();
        executor.shutdown();
        log.info("execution took {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
    }

    private static CompletableFuture<?>[] sendRequests(JettyClient jettyClient, ExecutorService executor) {
        return TaskList.tasks
                .mapToObj(taskId -> CompletableFuture.supplyAsync(sendRequest(jettyClient, taskId), executor))
                .toArray(CompletableFuture[]::new);
    }

    private static Supplier<String> sendRequest(JettyClient jettyClient, Integer taskId) {
        return ThrowingSupplier.unchecked(() -> jettyClient.syncSend(taskId));
    }
}
