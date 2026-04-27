package io.github.hungariannotation.steam.webapi.methods;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Models the response formats offered by the Steam APIs.
 */
public enum SteamApiFormat {
    JSON("json", "application/json"), XML("xml", "text/xml"), VDF("vdf", "text/vdf");

    private final String id;
    private final String mediaType;

    SteamApiFormat(String value, String mediaType) {
        this.id = value;
        this.mediaType = mediaType;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getId() {
        return id;
    }

    /**
     * Returns a representation of the argument required to request this format from
     * the Steam API.
     * 
     * @return
     */
    public Entry<String, String> request() {
        return Map.entry("format", id);
    }
}