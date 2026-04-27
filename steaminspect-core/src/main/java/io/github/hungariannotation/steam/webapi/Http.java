package io.github.hungariannotation.steam.webapi;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

/**
 * HTTP constants and utilities.
 */
public class Http {
    public static final String MEDIATYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";

    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    /**
     * Should be either an RFC 1123 Date-Time, or "0" to indicate the response is
     * already expired.
     * 
     * @see {@link DateTimeFormatter#RFC_1123_DATE_TIME}
     */
    public static final String HEADER_EXPIRES = "Expires";

    public static final String HEADER_DATE = "Date";

    private Http() {
    }

    public static HttpRequest.Builder getDefaultRequestBuilder(URI uri) {
        return HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(20))
                .header(HEADER_CONTENT_TYPE, MEDIATYPE_FORM_URLENCODED);
    }

}
