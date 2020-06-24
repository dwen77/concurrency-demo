package com.example.concurrent.httpclient;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpMethod;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Slf4j
public class JettyClient {

    private final HttpClient httpClient;

    public JettyClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CompletableFuture<String> callbackSend(Integer taskId) {
        log.info("send nio request {} ", taskId);
        CompletableFuture<String> httpResponseCompletableFuture = new CompletableFuture<>();
        httpClient.newRequest("http://127.0.0.1:8080/demo/ping/" + taskId)
                .method(HttpMethod.GET)
                .send(new BufferingResponseListener() {
                    @Override
                    public void onComplete(Result result) {
                        Throwable responseFailure = result.getResponseFailure();
                        if (responseFailure != null) {
                            httpResponseCompletableFuture.completeExceptionally(responseFailure);
                        } else {
                            String response = getContentAsString();
                            httpResponseCompletableFuture.complete(response);
                            log.info("receive response {} - {}", taskId, response);
                        }
                    }
                });
        return httpResponseCompletableFuture;
    }

    public String syncSend(Integer taskId) throws InterruptedException, ExecutionException, TimeoutException {
        log.info("send sync request {}", taskId);
        ContentResponse contentResponse = httpClient.newRequest("http://127.0.0.1:8080/demo/ping/" + taskId)
                .method(HttpMethod.GET)
                .send();
        String response = contentResponse.getContentAsString();
        log.info("receive response {} - {}", taskId, response);
        return response;
    }
}
