package io.github.hungariannotation.steam.webapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import io.github.hungariannotation.steam.kv.KvParser;
import io.github.hungariannotation.steam.kv.KvValue;
import io.github.hungariannotation.steam.webapi.methods.SteamApiFormat;

/**
 * Supports parsing Http responses from Steam APIs with {@code text/vdf}
 * content.
 */
public record KeyValueResponse(HttpResponse<?> response, Optional<KvValue> body) {

    private static final Pattern HEADER_SPLIT = Pattern.compile("\\s*;\\s*");

    private static boolean headerContains(List<String> headerValues, String value) {
        var all = headerValues.stream()
                .flatMap(each -> HEADER_SPLIT.splitAsStream(each)).toList();
        return all.stream().anyMatch(v -> v.equalsIgnoreCase(value));
    }

    private static boolean headerContains(HttpHeaders headers, String header, String value) {
        return headerContains(headers.allValues(header), value);
    }

    /**
     * Process an HttpReponse that may contain text/vdf encoded data.
     * 
     * @param response
     * @param throwable
     * @return
     */
    public static KeyValueResponse parseBody(HttpResponse<?> response) {
        if (response.statusCode() != 200) {
            throw new HttpResponseException(response);
        }

        KvValue value = null;

        if (headerContains(response.headers(), Http.HEADER_CONTENT_TYPE, SteamApiFormat.VDF.getMediaType())) {
            var body = response.body();

            if (body instanceof String stringBody) {
                value = KvParser.parse(stringBody);
            } else if (body instanceof byte[] byteArray) {
                try {
                    value = KvParser.parse(byteArray, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else if (body instanceof InputStream streamBody) {
                try (var rd = new InputStreamReader(streamBody)) {
                    value = KvParser.parse(rd);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }

        return new KeyValueResponse(response, Optional.ofNullable(value));
    }

}