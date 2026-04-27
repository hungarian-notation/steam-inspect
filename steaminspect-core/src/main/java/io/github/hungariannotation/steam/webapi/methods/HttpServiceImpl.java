package io.github.hungariannotation.steam.webapi.methods;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import io.github.hungariannotation.steam.webapi.HttpRequestHandler;

public class HttpServiceImpl implements HttpRequestHandler {

    private Supplier<HttpClient> client;

    public HttpServiceImpl(Supplier<HttpClient> client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<HttpResponse<byte[]>> requestAsync(HttpRequest argument) {
        return client.get().sendAsync(argument, BodyHandlers.ofByteArray());
    }

}
