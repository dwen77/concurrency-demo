package com.example.concurrent.parallel;

import com.example.concurrent.TaskList;
import com.example.concurrent.helper.ThrowingConsumer;
import com.example.concurrent.helper.ThrowingRunnable;
import com.example.concurrent.httpclient.JettyClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public class RunAsyncBlockingHttpClient {

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
        httpClient.setMaxConnectionsPerDestination(300);
        JettyClient jettyClient = new JettyClient(httpClient);
        httpClient.start();
        TaskList.tasks.boxed().parallel().forEach(sendRequest(jettyClient));
        stopWatch.stop();
        httpClient.stop();
        log.info("execution took {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
    }

    private static Consumer<Integer> sendRequest(JettyClient jettyClient) {
        return ThrowingConsumer.unchecked(taskId -> {
            log.info("submit task {}", taskId);
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(ThrowingRunnable.unchecked(() -> {
                jettyClient.syncSend(taskId);
            }));
            completableFuture.get();
        });
    }
}
