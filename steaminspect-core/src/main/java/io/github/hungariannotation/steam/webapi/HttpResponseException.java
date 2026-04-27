package io.github.hungariannotation.steam.webapi;

import java.net.http.HttpResponse;

public class HttpResponseException extends RuntimeException {

    private final HttpResponse<? extends Object> response;

    public HttpResponseException(HttpResponse<? extends Object> response) {
        super("server replied with " + response.statusCode());
        this.response = response;
    }

    public HttpResponseException(HttpResponse<? extends Object> response, String message) {
        super(message);
        this.response = response;
    }

    public HttpResponseException(HttpResponse<? extends Object> response, Throwable throwable) {
        super(throwable);
        this.response = response;
    }

    public HttpResponseException(HttpResponse<? extends Object> response, String message, Throwable throwable) {
        super(message, throwable);
        this.response = response;
    }

    public HttpResponse<? extends Object> getResponse() {
        return response;
    }
}
