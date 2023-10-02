package com.example.shoppingsystem.config;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DefaultHttpClientWrapper implements HttpClientWrapper {

    private final HttpClient httpClient;

    @Autowired
    public DefaultHttpClientWrapper(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public HttpResponse execute(HttpRequest request) throws IOException {
        HttpUriRequest httpUriRequest = (HttpUriRequest) request;
        return httpClient.execute(httpUriRequest);
    }
}
