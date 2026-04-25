>&nbsp;
>
>It's common belief in the nuclear industry that a good way to check for high-pressure steam leaks is by waving a broomstick in front of you: when the stick suddenly gets chopped in half, you've found your leak.
>
>&nbsp;

In that spirit I present Broomstick, a tool for finding Steam.

----------
# Overview

Broomstick is a utility library designed to allow for programmatic discovery and inspection of a user's Steam installation.

On Windows, the main Steam installation location is located empirically by inspecting the relevant registry key. Support for Linux is more limited, but discovery should still work if Steam is installed in a standard location.

Once Broomstick discovers the User's primary Steam library, it can follow metadata to the rest of the Steam installation locations on the machine, enumerating all installed games, their installation directories, and their downloaded workshop items.

Steam stores very little locally in its own formats about workshop items. To get even the name of a mod, I suspect I'd have to individually support every game's custom mod file structure. In lieu of that nightmare, I have implemented a rudimentary client for Steam's Web API that can query workshop deails from the `ISteamRemoteStorage/GetPublishedFileDetails` method. 

The client is currently extremely barebones for now, and it implements no caching or rate limiting.

