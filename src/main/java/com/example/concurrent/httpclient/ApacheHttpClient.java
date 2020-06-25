package com.example.concurrent.httpclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

@Slf4j
public class ApacheHttpClient {

    private final CloseableHttpClient httpClient;

    public ApacheHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String syncSend(Integer taskId) throws IOException {
        log.info("send sync request {}", taskId);
        HttpGet request = new HttpGet("http://127.0.0.1:8080/demo/ping/" + taskId);
        CloseableHttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            String result = EntityUtils.toString(entity);
            log.info("receive response {} - {}", taskId, result);
        }
        return null;
    }
}
