package io.github.hungariannotation.steam.webapi;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Formatter;

/**
 * Utility for printing a verbose rendering of an {@link HttpRequest} or
 * {@link HttpResponse} to an {@link OutputStream} or {@link Writer}
 */
public class MessagePrinter {

    /**
     * Abstraction of printf that lets us write to Streams, Writers, and Formatters
     * with the same function.
     */
    @FunctionalInterface
    public interface FormatFunction<T> {
        public T format(String format, Object... args);
    }

    public static String formatMessage(HttpRequest message) {
        var writer = new StringWriter();
        formatMessage(message, writer);
        return writer.toString();
    }

    public static String formatMessage(HttpResponse<String> message) {
        var writer = new StringWriter();
        formatMessage(message, writer);
        return writer.toString();
    }

    public static void formatMessage(HttpRequest message, FormatFunction<?> stream) {
        message.version().ifPresent(version -> stream.format("VERSION %s%n", version));
        stream.format("METHOD %s%n", message.method());
        stream.format("URI %s%n", message.uri());
        message.timeout().ifPresent(timeout -> stream.format("TIMEOUT %s%n", timeout));
        formatHeaders(message.headers(), stream);
        var subscriber = message.bodyPublisher().map(StringSubscriber::subscribeTo);
        subscriber.ifPresent((sub) -> stream.format("BODY %n%s%n", String.join(" ", sub.join())));
    }

    public static void formatMessage(HttpResponse<String> message, FormatFunction<?> stream) {
        stream.format("VERSION %s%n", message.version());
        stream.format("URI %s%n", message.uri());
        stream.format("STATUS %d%n", message.statusCode());
        formatHeaders(message.headers(), stream);
        stream.format("BODY %n%s%n", message.body());
    }

    public static void formatMessage(HttpRequest message, Formatter formatter) {
        formatMessage(message, formatter::format);
    }

    public static void formatMessage(HttpResponse<String> message, Formatter formatter) {
        formatMessage(message, formatter::format);
    }

    public static void formatMessage(HttpRequest message, Writer stream) {
        if (stream instanceof PrintWriter printStream) {
            formatMessage(message, printStream::printf);
        } else {
            PrintWriter printer = new PrintWriter(stream);
            formatMessage(message, printer::printf);
        }
    }

    public static void formatMessage(HttpResponse<String> message, Writer stream) {
        if (stream instanceof PrintWriter printStream) {
            formatMessage(message, printStream::printf);
        } else {
            PrintWriter printer = new PrintWriter(stream);
            formatMessage(message, printer::printf);
        }
    }

    public static void formatMessage(HttpRequest message, OutputStream stream) {
        if (stream instanceof PrintStream printStream) {
            formatMessage(message, printStream::printf);
        } else {
            PrintStream printer = new PrintStream(stream);
            formatMessage(message, printer::printf);
        }
    }

    public static void formatMessage(HttpResponse<String> message, OutputStream stream) {
        if (stream instanceof PrintStream printStream) {
            formatMessage(message, printStream::printf);
        } else {
            PrintStream printer = new PrintStream(stream);
            formatMessage(message, printer::printf);
        }
    }

    private static void formatHeaders(HttpHeaders headers, FormatFunction<?> stream) {
        for (var header : headers.map().entrySet()) {
            stream.format("HEADER %s: %s%n", header.getKey(), String.join("; ", header.getValue()));
        }
    }

}
