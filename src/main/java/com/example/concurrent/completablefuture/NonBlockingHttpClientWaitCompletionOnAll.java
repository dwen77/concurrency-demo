package com.example.concurrent.completablefuture;

import com.example.concurrent.TaskList;
import com.example.concurrent.helper.ThrowingFunction;
import com.example.concurrent.httpclient.JettyClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NonBlockingHttpClientWaitCompletionOnAll {

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
        log.info("max queue per destination {} " , httpClient.getMaxConnectionsPerDestination());
        JettyClient jettyClient = new JettyClient(httpClient);
        httpClient.start();
        CompletableFuture<Void> all = CompletableFuture.allOf(
                TaskList.tasks
                        .mapToObj(jettyClient::callbackSend)
                        .toArray(CompletableFuture[]::new))
                .exceptionally(ThrowingFunction.unchecked(throwable -> {
                    httpClient.stop();
                    throw throwable;
                }));
        all.join();
        stopWatch.stop();
        httpClient.stop();
        log.info("execution took {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
    }
}
