package io.github.hungariannotation.steam;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import io.soabase.recordbuilder.core.RecordBuilder;

/**
 * @param expires from the {@code Expires} header
 */
@RecordBuilder
@RecordBuilder.Options(addSingleItemCollectionBuilders = true, useImmutableCollections = true)
public record WorkshopDetails(
        String id,
        String creator,
        String appid,
        String title,
        String previewUrl,
        String description,
        Instant timeCreated,
        Instant timeUpdated,
        Instant expires,
        List<String> tags) implements Serializable {

    @Override
    public final String toString() {
        return String.format("%s [%s; %s]", WorkshopDetails.class.getName(), id, title);
    }
}