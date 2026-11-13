package de.raindancer.hyades.link;

import de.raindancer.client.protocol.ClientCodec;
import de.raindancer.client.protocol.ClientFeature;
import de.raindancer.client.protocol.FeatureRange;
import de.raindancer.client.protocol.FeatureVersion;
import de.raindancer.client.protocol.Hello;
import de.raindancer.client.protocol.Theme;
import de.raindancer.client.protocol.Welcome;
import de.raindancer.client.protocol.Wire;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClientLinkTest {

    private final List<byte[]> sent = new ArrayList<>();
    private long now = 1000;
    private final ClientLink link = new ClientLink(sent::add, () -> now, "0.1.0", "26.3");

    private static byte[] welcome(int protocol, FeatureVersion... agreed) {
        return ClientCodec.encode(new Welcome(protocol, List.of(agreed), "Hyades", Theme.HARBOR_SUNSET));
    }

    private static byte[] welcome() {
        return welcome(1, new FeatureVersion(ClientFeature.SESSION.id(), 1));
    }

    @Test
    void nothingIsSaidToAServerThatDidNotAnnounceTheChannel() {
        assertThat(sent).isEmpty();
        assertThat(link.connection()).isEmpty();
    }

    @Test
    void aServerThatListensGetsOneHello() {
        link.serverListens(3, "de_de");
        link.serverListens(3, "de_de");

        assertThat(sent).hasSize(1);
        assertThat(ClientCodec.decodeServerbound(sent.get(0))).contains(new Hello(Wire.PROTOCOL, "0.1.0", "26.3",
                List.of(FeatureRange.of(ClientFeature.SESSION)), 3, "de_de"));
        assertThat(link.connection()).isEmpty();
    }

    @Test
    void theWelcomeIsTheConnection() {
        link.serverListens(2, "en_us");
        now = 5000;
        link.received(welcome());

        ClientLink.Connection connection = link.connection().orElseThrow();
        assertThat(connection.serverName()).isEqualTo("Hyades");
        assertThat(connection.theme()).isEqualTo(Theme.HARBOR_SUNSET);
        assertThat(connection.agreed()).containsEntry(ClientFeature.SESSION, 1);
        assertThat(connection.since()).isEqualTo(5000);
    }

    @Test
    void aWelcomeNobodyAskedForIsIgnored() {
        link.received(welcome());
        assertThat(link.connection()).isEmpty();
    }

    @Test
    void aWelcomeWithoutTheSessionOrInAStrangeProtocolIsIgnored() {
        link.serverListens(2, "en_us");
        link.received(welcome(1));
        link.received(welcome(0, new FeatureVersion(ClientFeature.SESSION.id(), 1)));
        link.received(welcome(Wire.PROTOCOL + 1, new FeatureVersion(ClientFeature.SESSION.id(), 1)));
        link.received(welcome(1, new FeatureVersion(ClientFeature.SESSION.id(), 9)));
        assertThat(link.connection()).isEmpty();
    }

    @Test
    void whatTheServerAgreedButThisModDoesNotKnowIsLeftOut() {
        link.serverListens(2, "en_us");
        link.received(welcome(1, new FeatureVersion(ClientFeature.SESSION.id(), 1), new FeatureVersion(4711, 3)));
        assertThat(link.connection().orElseThrow().agreed()).containsOnlyKeys(ClientFeature.SESSION);
    }

    @Test
    void garbageFromAServerIsIgnored() {
        link.serverListens(2, "en_us");
        link.received(new byte[0]);
        link.received(new byte[]{0, 0, 1, 9, 9});
        link.received(new byte[]{99, 1, 2});
        assertThat(link.connection()).isEmpty();
    }

    @Test
    void theNextServerGetsItsOwnHello() {
        link.serverListens(2, "en_us");
        link.received(welcome());
        link.disconnected();
        assertThat(link.connection()).isEmpty();

        link.serverListens(2, "en_us");
        assertThat(sent).hasSize(2);
        assertThat(link.connection()).isEmpty();
    }
}
