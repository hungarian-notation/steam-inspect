package io.github.hungariannotation.steam.webapi.methods;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import io.github.hungariannotation.steam.webapi.HttpRequestHandler;

public class ClientProvider implements Supplier<HttpClient>, HttpRequestHandler {

    private final Object lock = new Object();
    private volatile HttpClient client = null;
    private volatile boolean closed = true;

    private final Supplier<HttpClient> factory;

    public ClientProvider(Supplier<HttpClient> factory) {
        this.factory = factory;
    }

    public HttpClient get() {
        if (closed || this.client == null || this.client.isTerminated()) {
            synchronized (lock) {
                if (this.client == null || this.client.isTerminated()) {
                    this.client = this.factory.get();
                    this.closed = false;
                    return this.client;
                }
            }
        }

        return this.client;
    }

    @Override
    public CompletableFuture<HttpResponse<byte[]>> requestAsync(HttpRequest argument) {
        return get().sendAsync(argument, BodyHandlers.ofByteArray());
    }

}
