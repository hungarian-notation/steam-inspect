package io.github.hungariannotation;

import io.github.hungariannotation.steam.SteamInspect;

public class ListWorkshopItems {
    
    private ListWorkshopItems() {
        
    }

    public static void main(String[] args) {
        var steam = SteamInspect.getSteam();

        for (var app : steam.getApps()) {
            System.out.println(app.getName());
            for (var item : app.getWorkshopItems()) {

                // Synchronously query the Steam API for details about this item.
                var details = item.getDetails();

                // The implementation actually retrieves all workshop details for a given appid
                // in the same request, so only the first call to getDetails() for items from a
                // given game will block.

                details.ifPresentOrElse((d) -> {
                    System.out.println("    " + item.getId() + " " + d.title());
                }, () -> {
                    System.out.println("    " + item.getId());
                });
            }
        }
    }
}
