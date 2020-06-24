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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public class CustomThreadPoolRunAsyncMixedHttpClient {

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
        TaskList.tasks.boxed().parallel().forEach(sendRequest(jettyClient, executorService));
        stopWatch.stop();
        httpClient.stop();
        executorService.shutdown();
        log.info("execution took {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
    }

    private static Consumer<Integer> sendRequest(JettyClient httpClient, ExecutorService executorService) {
        return ThrowingConsumer.unchecked(taskId -> {
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(
                    ThrowingRunnable.unchecked(
                            () -> {
                                log.info("submit task {}", taskId);
                                if (taskId % 2 == 0) {
                                    httpClient.syncSend(taskId);
                                } else {
                                    httpClient.callbackSend(taskId);
                                }
                            }
                    ), executorService
            );
            completableFuture.get();
        });
    }
}
