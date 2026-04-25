package hafnium.steam.impl;

import com.sun.jna.Platform;

import hafnium.steam.Steam;
import hafnium.steam.impl.win32.Win32Steam;

public class SteamLibraryNative {
    private SteamLibraryNative() {
    }

    private static Steam instance;

    /**
     * Gets an appropriate plaform-specific implemenation of SteamInstallation.
     * 
     * @return
     */
    public static Steam getInstance() {
        if (instance == null) {
            if (Platform.isWindows()) {
                instance = new Win32Steam();
            } else {
                instance = new FallbackSteam();
            }
        }

        return instance;
    }
}
