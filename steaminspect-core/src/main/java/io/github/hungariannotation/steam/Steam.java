package io.github.hungariannotation.steam;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Represents the Steam installation on the current machine, including all game
 * library roots.
 */
public interface Steam {

    /**
     * Get the main steam library folder. The Windows implementation retrieves this
     * programmatically from the registry, while the fallback implementation searches
     * various paths Steam may be installed on different systems.
     * 
     * @return
     */
    Optional<Path> getSteam();

    /**
     * {@code Steam/steamapps/libraryfolders.vdf} contains a record for every Steam
     * library folder and a list of the appids of the games installed there.
     * 
     * The Steam directory we're under is one of them, but there may be others.
     * 
     * @return
     */
    List<? extends SteamLibraryFolder> getLibraryFolders();

    /**
     * Get a list of all installed Steam apps.
     * 
     * @return
     */
    List<SteamApp> getApps();

    /**
     * Get a specific Steam app, or an empty optional if it is not installed.
     * 
     * @param id
     * @return
     */
    Optional<SteamApp> getApp(String id);

}