package io.github.hungariannotation.steam.webapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import io.github.hungariannotation.steam.webapi.methods.SteamApi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

// @Disabled
public class SteamApiTest {

    private static final String TEST_MOD = "100";
    private static final String TEST_MOD_NAME = "Singed Scythe";
    private static final String TEST_MOD_CREATOR = "76561198001258350";

    @Test()
    @Tag("integration")
    @Tag("network-test")
    void testGetPublishedFileDetails() {
        // This test depends on none of the details of this primordial workshop item
        // remaining constant:
        //
        // https://steamcommunity.com/workshop/filedetails/?id=100
        //
        // It should generally be disabled, as it makes HTTP requests against Steam's
        // API, and a failure might be the result of a change to the mod's workshop
        // page.

        var futureResponse = SteamApi.getInstance().getWorkshopDetailsMethod().requestAsync(List.of(TEST_MOD));
        var response = futureResponse.join();

        assertEquals(1, response.size());
        var mod = response.get(TEST_MOD);

        assertNotNull(mod);
        assertTrue(Instant.now().compareTo(mod.expires()) < 0,
                "unexpected expiration time: "
                        + DateTimeFormatter.RFC_1123_DATE_TIME
                                .format(mod.expires()
                                        .atZone(ZoneId.systemDefault())));
        assertEquals(TEST_MOD_NAME, mod.title());
        assertEquals(TEST_MOD_CREATOR, mod.creator());
    }

    @Test()
    @Tag("integration")
    @Tag("network-test")
    void getLargeList() {
        var resource = this.getClass().getResourceAsStream("modlist.txt");
        var mods = new ArrayList<String>();

        try (var reader = new BufferedReader(new InputStreamReader(resource))) {
            reader.readAllLines()
                    .stream()
                    .map(String::trim)
                    .filter(str -> str.length() > 0)
                    .forEach(mods::add);
        } catch (IOException e) {
            fail(e);
        }

        // System.out.println(mods);

        var futureResponse = SteamApi.getInstance().getWorkshopDetailsMethod().requestAsync(mods);
        var response = futureResponse.join();
        assertEquals(mods.size(), response.size());

        for (var id : mods) {
            assertTrue(response.containsKey(id));
        }

        // response.forEach((k, v) -> {
        // var keepTime = v.expires();
        // var keepDuration = Duration.between(Instant.now(),keepTime);
        // var localExpireTime = v.expires().atZone(ZoneId.systemDefault());
        // var format = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        // System.out.printf("%s: %s;%n\tretain until %s%n\t(%dh %dm %ds)%n%n", k,
        // v.title(), format.format(localExpireTime),
        // keepDuration.toHours(),
        // keepDuration.toMinutesPart(),
        // keepDuration.toSecondsPart());
        // });

    }
}
