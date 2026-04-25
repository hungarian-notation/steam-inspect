module hafnium.steam {
    requires java.base;
    requires java.net.http;
    requires com.sun.jna; // for windows registry access
    requires com.sun.jna.platform;
    requires jakarta.json;
    requires jakarta.json.bind;

    uses jakarta.json.spi.JsonProvider; // requires org.eclipse.parsson;
    uses jakarta.json.bind.spi.JsonbProvider; // requires org.eclipse.yasson;

    opens hafnium.steam;

    exports hafnium.steam;
    exports hafnium.steam.kv;
}
