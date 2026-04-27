package io.github.hungariannotation.steam;

import java.nio.file.Path;
import java.util.Optional;

import io.github.hungariannotation.steam.impl.BaseSteamLibrary;
import io.github.hungariannotation.steam.impl.SteamLibraryNative;

/**
 * Provides static factories for {@link Steam} instances.
 */
public class SteamInspect {

    /**
     * Gets an appropriate platform-specific implementation of SteamInstallation.
     * 
     * @return
     */
    public static Steam getSteam() {
        return SteamLibraryNative.getInstance();
    }

    /**
     * Returns a SteamLibrary interface for the steam installation at a given path.
     * 
     * @param path
     * @return
     */
    public static Steam getSteam(Path path) {
        return new BaseSteamLibrary() {
            @Override
            public Optional<Path> getSteam() {
                return Optional.ofNullable(path);
            }
        };
    }

}
