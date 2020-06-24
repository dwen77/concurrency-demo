package com.example.concurrent.completablefuture;

import com.example.concurrent.TaskList;
import com.example.concurrent.helper.ThrowingFunction;
import com.example.concurrent.helper.ThrowingRunnable;
import com.example.concurrent.httpclient.JettyClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CustomThreadPoolMixedHttpClient {

    public static void main(String[] args) throws Exception {
        run();
    }

    private static void run() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        HttpClient httpClient = new HttpClient(sslContextFactory);
        JettyClient jettyClient = new JettyClient(httpClient);
        httpClient.start();
        ExecutorService executorService = Executors.newCachedThreadPool();
        CompletableFuture<Void> all = CompletableFuture.allOf(
                TaskList.tasks
                        .mapToObj(taskId -> sendRequest(jettyClient, taskId, executorService))
                        .toArray(CompletableFuture[]::new))
                .exceptionally(ThrowingFunction.unchecked(throwable -> {
                    log.error("failed to send request due to {}", throwable.getMessage());
                    httpClient.stop();
                    executorService.shutdown();
                    throw throwable;
                }));
        all.join();
        stopWatch.stop();
        httpClient.stop();
        executorService.shutdown();
        log.info("execution took {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
    }

    private static CompletableFuture<?> sendRequest(JettyClient httpClient, Integer taskId, ExecutorService executorService) {
        return CompletableFuture.runAsync(ThrowingRunnable.unchecked(() -> {
            log.info("submit task {}", taskId);
            if (taskId % 2 == 0) {
                httpClient.syncSend(taskId);
            } else {
                CompletableFuture<String> callbackSend = httpClient.callbackSend(taskId);
                callbackSend.get();
            }
        }), executorService);
    }
}
