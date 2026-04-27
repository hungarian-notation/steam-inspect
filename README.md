
----------
# Overview

SteamInspect is a utility library designed to allow for programmatic discovery and inspection of a user's Steam installation.

On Windows, the main Steam installation location is located empirically by inspecting the relevant registry key. Support for Linux is more limited, but discovery should still work if Steam is installed in a standard location.

Once SteamInspect discovers the User's primary Steam library, it can follow metadata to the rest of the Steam installation locations on the machine, enumerating all installed games, their installation directories, and their downloaded workshop items.

Steam stores very little locally in its own formats about workshop items. To get even the name of a mod, I suspect I'd have to individually support every game's custom mod file structure. In lieu of that nightmare, I have implemented a client for Steam's Web API that Most of the endpoints require some level of authentication, so we are currently only interfacing with the workshop, querying workshop details from the `ISteamRemoteStorage/GetPublishedFileDetails` method.

The networking implementation now implements a simple cache, governed by the `expires` directives returned from the API.  