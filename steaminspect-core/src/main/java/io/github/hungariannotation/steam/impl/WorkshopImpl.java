package io.github.hungariannotation.steam.impl;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.github.hungariannotation.steam.SteamApp;
import io.github.hungariannotation.steam.WorkshopDetails;
import io.github.hungariannotation.steam.WorkshopItem;
import io.github.hungariannotation.steam.kv.KvParser;
import io.github.hungariannotation.steam.kv.KvTable;
import io.github.hungariannotation.steam.kv.KvValue;
import io.github.hungariannotation.steam.webapi.methods.SteamApi;

public class WorkshopImpl implements SteamApp.SteamAppWorkshop {

    private final SteamAppImpl app;
    private final List<WorkshopItemImpl> items;
    private final Optional<Instant> timeLastUpdated;
    private final Optional<Instant> timeLastAppRan;

    public WorkshopImpl(SteamAppImpl app, List<WorkshopItemImpl.ManifestData> items,
            Optional<Instant> timeLastUpdated,
            Optional<Instant> timeLastAppRan) {
        this.app = app;
        this.items = items
                .stream()
                .map(data -> new WorkshopItemImpl(this, data))
                .collect(Collectors.toUnmodifiableList());
        this.timeLastUpdated = timeLastUpdated;
        this.timeLastAppRan = timeLastAppRan;
    }

    public static Optional<WorkshopImpl> read(SteamAppImpl app, Optional<Path> workshopManifest) {
        return KvParser.read(workshopManifest)
                .flatMap(data -> data.getTable("AppWorkshop")
                        .map(rootTable -> fromRootTable(app, rootTable)));
    }

    private static WorkshopImpl fromRootTable(SteamAppImpl app, KvTable rootTable) {
        var items = rootTable.getTable("WorkshopItemDetails").stream()
                .flatMap(KvTable::streamEntries)
                .map(entry -> toWorkshopItem(app, entry))
                .toList();
        var updated = Timestamp.getInstant(rootTable.getString("TimeLastUpdated"));
        var ran = Timestamp.getInstant(rootTable.getString("TimeLastAppRan"));
        return new WorkshopImpl(app, items, updated, ran);
    }

    private static WorkshopItemImpl.ManifestData toWorkshopItem(SteamAppImpl app, Entry<String, KvValue> item) {
        var workshopId = item.getKey();
        var table = item.getValue();
        var manifestLocal = table.getString("manifest").orElse("0");
        var manifestRemote = table.getString("latest_manifest").orElse("0");
        var updatedLocal = table.getString("timeupdated").orElse("0");
        var updatedRemote = table.getString("latest_timeupdated").orElse("0");
        var touched = table.getString("timetouched").orElse("0");
        var result = new WorkshopItemImpl.ManifestData(
                workshopId,
                touched, updatedLocal, updatedRemote,
                manifestLocal, manifestRemote);
        return result;
    }

    private CompletableFuture<Map<String, WorkshopDetails>> requestDetails() {
        return SteamApi.getInstance().getWorkshopDetailsMethod()
                .requestAsync(this.items
                        .stream()
                        .map(WorkshopItemImpl::getId)
                        .toList());
    }

    private final Memoized<CompletableFuture<Map<String, WorkshopDetails>>> remoteItemData = Memoized
            .of(this::requestDetails);

    /**
     * Asynchronously retrieves data about the given workshop item.
     * <p>
     * This implementation retrieves data about <b>all</b> workshop items the first
     * time details are requests for any item.
     * 
     * @param item
     * @return
     */
    CompletableFuture<Optional<WorkshopDetails>> getDetailsAsync(WorkshopItem item) {
        return remoteItemData.get().thenApply(results -> {
            return Optional.ofNullable(results.get(item.getId()));
        });
    }

    /**
     * Like {@link #getDetailsAsync(WorkshopItem)}, but blocks until the request to
     * the API resolves.
     * 
     * @param item
     * @return
     */
    Optional<WorkshopDetails> getDetails(WorkshopItem item) {
        return getDetailsAsync(item).join();
    }

    @Override
    public SteamApp getApp() {
        return this.app;
    }

    @Override
    public List<? extends WorkshopItem> getItems() {
        return this.items;
    }

    @Override
    public Optional<Instant> getTimeLastUpdated() {
        return this.timeLastUpdated;
    }

    @Override
    public Optional<Instant> getTimeLastAppRan() {
        return this.timeLastAppRan;
    }

}