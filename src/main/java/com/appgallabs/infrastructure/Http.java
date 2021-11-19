package com.appgallabs.infrastructure;

import javax.inject.Singleton;
import java.net.http.HttpClient;

@Singleton
public class Http {

    private HttpClient httpClient = HttpClient.newBuilder().build();

    public HttpClient getHttpClient() {
        return httpClient;
    }
}
