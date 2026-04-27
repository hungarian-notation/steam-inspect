package io.github.hungariannotation.steam.webapi;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscription;

/**
 * A utility that subscribes to a {@link Flow.Publisher} and collects its output
 * as a list of strings.
 * <p>
 * This subscriber immediately requests all data from the publisher. When
 * passing this subscriber to a publisher, the call to
 * {@link Flow.Publisher#subscribe(java.util.concurrent.Flow.Subscriber)
 * Flow.Publisher::subscribe} method may not return until after the publisher is
 * exhausted.
 * <p>
 * If that is undesirable, you can use {@link #subscribeTo(Publisher, Charset)}
 * to subscribe asynchronously.
 */
public class StringSubscriber<T> implements Flow.Subscriber<T> {

    private final Object lock = new Object();
    private volatile Subscription subscription;
    private final List<String> strings = Collections.synchronizedList(new ArrayList<String>());
    private final CompletableFuture<List<String>> completion;
    private final Charset charset;

    /**
     * @param charset Charset to use if the publisher deals in raw bytes.
     */
    public StringSubscriber(Charset charset) {
        this.charset = charset;
        this.completion = new CompletableFuture<>();
    }

    /**
     * Use UTF-8 if the publisher deals in raw bytes.
     */
    public StringSubscriber() {
        this(StandardCharsets.UTF_8);
    }

    /**
     * Subscribe to the given publisher asynchronously.
     * <p>
     * This method can be used to retrieve a String from a publisher fluently via
     * the following idiom:
     * 
     * <pre>{@code
     * String value = StringSubscriber.subscribeTo(publisher).join();
     * }</pre>
     * 
     * @param <T>
     * @param publisher
     * @param charset
     * @return A {@link CompletableFuture} that will not complete until the created
     *         subscriber is completed by the publisher.
     */
    public static <T> CompletableFuture<List<String>> subscribeTo(Publisher<T> publisher, Charset charset) {
        StringSubscriber<T> subscriber = new StringSubscriber<>();
        CompletableFuture.runAsync(() -> {
            publisher.subscribe(subscriber);
        });
        return subscriber.getCompletion();
    }

    /**
     * Invokes {@link #subscribeTo(Publisher, Charset)} using the
     * {@link StandardCharsets#UTF_8} charset.
     * 
     * @param <T>
     * @param publisher
     * @return
     */
    public static <T> CompletableFuture<List<String>> subscribeTo(Publisher<T> publisher) {
        return subscribeTo(publisher, StandardCharsets.UTF_8);
    }

    /**
     * @return a CompletableFuture that will complete after a publisher invokes
     *         {@link #onComplete()} on this instance, or complete exceptionally if
     *         {@link #onError(Throwable)} is invoked or an exception occurs while
     *         translating the published objects into Strings.
     *         <p>
     *         The only expected exception completion caused by logic internal to
     *         this class
     */
    public CompletableFuture<List<String>> getCompletion() {
        return completion.copy();
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        synchronized (lock) {
            if (this.subscription != null)
                throw new IllegalStateException("already subscribed");
            this.subscription = subscription;
            subscription.request(Long.MAX_VALUE);
        }
    }

    @Override
    public void onNext(T object) {
        try {
            if (object instanceof CharSequence sequence) {
                strings.add(sequence.toString());
            } else if (object instanceof byte[] bytes) {
                strings.add(charset.newDecoder()
                        .decode(ByteBuffer.wrap(bytes))
                        .toString());
            } else {
                // Fallback
                strings.add(String.valueOf(object));
            }
        } catch (Exception e) {
            completion.completeExceptionally(e);
            subscription.cancel();
        }
    }

    @Override
    public void onError(Throwable throwable) {
        completion.completeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
        completion.complete(List.copyOf(strings));
    }

}
