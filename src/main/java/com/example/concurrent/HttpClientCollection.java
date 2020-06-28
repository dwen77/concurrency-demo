package com.example.concurrent;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpClientCollection {

    static Map<Integer, HttpClient> httpClientMap = new ConcurrentHashMap<>();
    

    static {
       TaskList.taskStreamSupplier.get().forEach(HttpClientCollection::populateMap);
    }

   private static void populateMap(int task) {
      SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
      HttpClient httpClient = new HttpClient(sslContextFactory);
       try {
           httpClient.start();
       } catch (Exception e) {
           e.printStackTrace();
       }
       httpClientMap.put(task, httpClient);
   }

   public static HttpClient getHttpClient(int task) {
       return httpClientMap.get(task);
   }
}
