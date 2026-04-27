package io.github.hungariannotation.steam.webapi;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Abstracts the details of encoding and decoding an API request and response.
 * 
 * @param <T> Argument Type
 * @param <U> Response Type
 * @param <V> Underlying HttpResponse body type, exposed for testing.
 */
public interface RequestHandler<T, U> {

    /**
     * Send an asynchronous request.
     */
    public CompletableFuture<U> requestAsync(T argument);

    /**
     * Send a request and block until a reponse is received.
     * 
     * @param argument
     * @param client
     * @return
     */
    public default U requestSync(T argument) {
        return requestAsync(argument).join();
    }

    /**
     * Extend this service to accept a different type.
     * 
     * @param <TX>
     * @param extension
     * @return
     */
    public default <TX> RequestHandler<TX, U> mapRequest(
            Function<? super TX, T> extension) {
        return arg -> requestAsync(extension.apply(arg));
    }

    /**
     * Transform this service's result.
     * 
     * @param <UX>
     * @param extension
     * @return
     */
    public default <UX> RequestHandler<T, UX> mapResult(
            Function<? super U, UX> extension) {
        return arg -> requestAsync(arg).thenApply(extension);
    }

    /**
     * Transform this service's result given the request as context.
     * 
     * @param <UX>
     * @param extension
     * @return
     */
    public default <UX> RequestHandler<T, UX> mapResult(
            BiFunction<? super T, ? super U, UX> extension) {
        return arg -> requestAsync(arg).thenApply(result -> extension.apply(arg, result));
    }

}
