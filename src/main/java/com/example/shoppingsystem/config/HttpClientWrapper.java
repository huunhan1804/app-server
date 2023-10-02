package com.example.shoppingsystem.config;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import java.io.IOException;

public interface HttpClientWrapper {
    HttpResponse execute(HttpRequest request) throws IOException;
}
