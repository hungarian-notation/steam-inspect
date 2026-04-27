package io.github.hungariannotation.steam.webapi.methods;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * Container for API methods.
 */
public class SteamApi {
    private static final SteamApi GLOBAL = new SteamApi();

    public static SteamApi getInstance() {
        return GLOBAL;
    }

    private static final HttpClient getDefaultClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    private final ClientProvider clients;

    private final GetPublishedFileDetails workshopDetailsMethod;

    public SteamApi() {
        this(SteamApi::getDefaultClient);
    }

    public SteamApi(Supplier<HttpClient> factory) {
        this.clients = new ClientProvider(factory);
        this.workshopDetailsMethod = new GetPublishedFileDetails(this.clients);
    }

    public GetPublishedFileDetails getWorkshopDetailsMethod() {
        return workshopDetailsMethod;
    }

}
