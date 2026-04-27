package io.github.hungariannotation.steam.webapi;

import java.net.URLEncoder;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import static java.util.stream.Collectors.joining;

/**
 * Utility for constructing query arguments. Can be rendered to string via
 * {@link #render()} for use as a URI query, or wrapped in a
 * {@link BodyPublisher} with {@link #publish(Charset)} for use as a POST body.
 * <p>
 * This class is not thread safe, but the publishers returned by it are.
 */
public class Query {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final LinkedList<Entry<String, String>> list;

    /**
     * Construct an instance from an existing list of arguments.
     * 
     * @param arguments
     */
    public Query(List<Entry<String, String>> arguments) {
        this.list = new LinkedList<>(arguments);
    }

    /**
     * Construct an empty instance.
     */
    public Query() {
        this.list = new LinkedList<>();
    }

    /**
     * Adds an argument.
     * 
     * @param key
     * @param value
     * @return
     */
    public Query add(String key, String value) {
        this.list.add(Map.entry(key, value));
        return this;
    }

    /**
     * Adds an argument.
     * 
     * @param key
     * @param value
     * @return
     */
    public Query add(Entry<String, String> argument) {
        this.list.add(argument);
        return this;
    }

    /**
     * 
     * Adds "array" arguments.
     * <p>
     * Adds:
     * <ul>
     * <li>An array length argument with key {@code {countParameter}} and a value
     * equal to {@code values.size()}</li>
     * <li>An additional argument for every value {@code i = 0..size} in
     * {@code values} with key {@code {itemParameter}[{i}] }
     * </ul>
     * Example
     * 
     * <pre>{@code
     * jshell> new Query().addArray("arr", "len", List.of("a", "b", "c")).render(false)
     * $1 ==> "len=3&arr[0]=a&arr[1]=b&arr[2]=c" 
     * }</pre>
     * 
     * @param itemParameter
     * @param countParameter
     * @param values
     * @return
     */
    public Query addArray(String itemParameter, String countParameter, List<? extends Object> values) {
        add(countParameter, Integer.toString(values.size()));

        for (int i = 0; i < values.size(); ++i) {
            var name = itemParameter + "[" + Integer.toString(i) + "]";
            this.add(name, values.get(i).toString());
        }

        return this;
    }

    /**
     * Returns a BodyPublisher for the current arguments.
     * 
     * @return
     */
    public BodyPublisher publish(Charset charset) {
        return BodyPublishers.ofString(render(), charset);
    }

    /**
     * Returns a BodyPublisher for the current arguments.
     * 
     * @return
     */
    public BodyPublisher publish() {
        return publish(StandardCharsets.UTF_8);
    }

    public String render() {
        return render(true);
    }

    public String render(boolean urlEncode) {
        return list.stream()
                .map(each -> Stream.of(each.getKey(), each.getValue())
                        .map(t -> urlEncode ? URLEncoder.encode(t, CHARSET) : t)
                        .collect(joining("=")))
                .collect(joining("&"));
    }

}
