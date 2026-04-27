package io.github.hungariannotation.steam.webapi;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * An {@link RequestHandler} that deals directly in {@link HttpRequest} and
 * {@link HttpResponse} objects.
 */
public interface HttpRequestHandler extends RequestHandler<HttpRequest, HttpResponse<byte[]>> {
}