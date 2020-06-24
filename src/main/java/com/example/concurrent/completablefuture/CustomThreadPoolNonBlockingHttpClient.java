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
public class CustomThreadPoolNonBlockingHttpClient {

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
        ExecutorService executor = Executors.newCachedThreadPool();
        CompletableFuture<Void> all = CompletableFuture.allOf(
                TaskList.tasks
                        .mapToObj(taskId -> CompletableFuture.runAsync(sendRequest(jettyClient, taskId), executor))
                        .toArray(CompletableFuture[]::new))
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

    private static Runnable sendRequest(JettyClient jettyClient, Integer taskId) {
        return ThrowingRunnable.unchecked(
                () -> {
                    CompletableFuture<String> completableFuture = jettyClient.callbackSend(taskId);
                    completableFuture.get();
                }
        );
    }
}
