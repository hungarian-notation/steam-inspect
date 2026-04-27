package io.github.hungariannotation.steam.webapi.methods;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.hungariannotation.steam.WorkshopDetails;
import io.github.hungariannotation.steam.WorkshopDetailsBuilder;
import io.github.hungariannotation.steam.cache.CachedValueTraits;
import io.github.hungariannotation.steam.cache.SimpleCache;
import io.github.hungariannotation.steam.kv.KvTable;
import io.github.hungariannotation.steam.kv.KvValue;
import io.github.hungariannotation.steam.webapi.Http;
import io.github.hungariannotation.steam.webapi.HttpResponseException;
import io.github.hungariannotation.steam.webapi.HttpRequestHandler;
import io.github.hungariannotation.steam.webapi.KeyValueResponse;
import io.github.hungariannotation.steam.webapi.Query;

public class GetPublishedFileDetails implements MethodHandler<List<String>, Map<String, WorkshopDetails>> {

    static final String SCHEME = "https";
    static final String HOST = "api.steampowered.com";
    static final String PATH = "/ISteamRemoteStorage/GetPublishedFileDetails/v1/";
    static final URI METHOD;

    private final HttpRequestHandler composed;

    public GetPublishedFileDetails(HttpRequestHandler composed) {
        this.composed = composed;
    }

    static {
        try {
            METHOD = new URI(SCHEME, HOST, PATH, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URI getURI() {
        return getURI();
    }

    private final SimpleCache<String, WorkshopDetails> cache = new SimpleCache<>(CachedValueTraits
            .builderFor(WorkshopDetails.class)
            .expiration(WorkshopDetails::expires)
            .keys(WorkshopDetails::id));

    @Override
    public CompletableFuture<Map<String, WorkshopDetails>> requestAsync(List<String> argument) {
        var cached = cache.queryCache(argument);
        var cachedMap = cached.cached();

        if (cached.missed().size() == 0) {
            assert cachedMap.size() == argument.size() : "size mismatch";
            return CompletableFuture.completedFuture(cachedMap);
        }

        List<String> remaining = argument;

        if (cachedMap.size() > 0) {
            remaining = remaining.stream().filter(key -> !cachedMap.containsKey(key)).toList();
        }

        var request = getPublishedFileDetailsRequest(SteamApiFormat.VDF, argument);
        var response = composed.requestAsync(request);
        return response
                .thenApply(KeyValueResponse::parseBody)
                .thenApply(r -> processDetailsResponse(r))
                .thenApply(r -> {
                    cache.updateCache(r.values());
                    return r;
                });
    }

    private static Map<String, WorkshopDetails> processDetailsResponse(KeyValueResponse r) {
        var kv = r.body()
                .orElseThrow(() -> new HttpResponseException(r.response(),
                        "missing expected vdf content"));
        var root = kv.getTable("response");
        var resultCode = root.flatMap(v -> v.getString("result"));
        var isValid = resultCode.map(result -> result.equals("1")).orElse(false);
        if (!isValid) {
            throw new HttpResponseException(r.response(), "invalid result code: " + resultCode);
        }
        var array = root.flatMap(v -> v.getTable("publishedfiledetails"))
                .map(v -> v.streamValues())
                .orElse(Stream.empty())
                .flatMap(v -> v.asTable().stream())
                .map(t -> toWorkshopDetails(t, r))
                .collect(Collectors.toMap(v -> v.id(), v -> v));
        return array;
    }

    /**
     * Converts the given table into a {@link WorkshopDetails} object.
     */
    public static WorkshopDetails toWorkshopDetails(KvTable table, KeyValueResponse response) {
        var expires = response.response()
                .headers()
                .firstValue("Expires")
                .filter(val -> val.length() > 1)
                .map(GetPublishedFileDetails::parseDateRfc1123).orElse(Instant.MIN);

        var builder = WorkshopDetailsBuilder.builder()
                .expires(expires)
                .id(table.getString("publishedfileid").orElse(""))
                .creator(table.getString("creator").orElse(""))
                .appid(table.getString("consumer_app_id").orElse(""))
                .previewUrl(table.getString("preview_url").orElse(""))
                .title(table.getString("title").orElse(""))
                .description(table.getString("description").orElse(""))
                .timeCreated(table.getString("time_created")
                        .map(Long::parseLong)
                        .map(Instant::ofEpochSecond)
                        .orElse(null))
                .timeUpdated(table.getString("time_updated")
                        .map(Long::parseLong)
                        .map(Instant::ofEpochSecond)
                        .orElse(null));
        table.streamValuesOf("tags")
                .map(KvValue::asTable)
                .flatMap(Optional::stream)
                .map(tag -> tag.getString("tag"))
                .flatMap(Optional::stream)
                .forEach(builder::addTags);
        return builder.build();
    }

    public static Instant parseDateRfc1123(String value) {
        return ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
    }

    public static HttpRequest getPublishedFileDetailsRequest(SteamApiFormat format, List<String> items) {
        var builder = Http.getDefaultRequestBuilder(METHOD);
        builder.POST(new Query()
                .add(format.request())
                .addArray("publishedfileids", "itemcount", items)
                .publish());
        return builder.build();
    }

}
