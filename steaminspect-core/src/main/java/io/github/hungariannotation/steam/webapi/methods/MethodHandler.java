package io.github.hungariannotation.steam.webapi.methods;

import java.net.URI;

import io.github.hungariannotation.steam.webapi.RequestHandler;

public interface MethodHandler<T, U> extends RequestHandler<T, U> {

    URI getURI();

}
