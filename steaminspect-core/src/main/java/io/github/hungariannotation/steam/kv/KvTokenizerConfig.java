package io.github.hungariannotation.steam.kv;

import java.util.Optional;

public interface KvTokenizerConfig {

    Optional<Boolean> getEnableHexEscapes();

    Optional<Boolean> getEnableUnicodeEscapes();

}
