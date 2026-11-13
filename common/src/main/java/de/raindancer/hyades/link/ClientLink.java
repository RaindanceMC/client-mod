package de.raindancer.hyades.link;

import de.raindancer.client.protocol.ClientCodec;
import de.raindancer.client.protocol.ClientFeature;
import de.raindancer.client.protocol.FeatureRange;
import de.raindancer.client.protocol.FeatureVersion;
import de.raindancer.client.protocol.Hello;
import de.raindancer.client.protocol.MalformedPacketException;
import de.raindancer.client.protocol.Theme;
import de.raindancer.client.protocol.Welcome;
import de.raindancer.client.protocol.Wire;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongSupplier;

/**
 * This client's side of the channel, for one connection at a time.
 *
 * <p>Nothing is ever sent to a server that did not announce {@link Wire#CHANNEL}: on any other server the mod
 * is silent and draws nothing. The game calls in from its network thread and from its render thread, hence
 * the locking.
 */
public final class ClientLink {

    @FunctionalInterface
    public interface Outbox {
        void send(byte[] frame);
    }

    /**
     * A server that answered.
     *
     * @param agreed what it may use, each at the one version to speak
     * @param since  when the welcome came, by this link's clock
     */
    public record Connection(String serverName, Theme theme, Map<ClientFeature, Integer> agreed, long since) {

        public Connection {
            agreed = Map.copyOf(agreed);
        }
    }

    private final Outbox outbox;
    private final LongSupplier clock;
    private final String modVersion;
    private final String minecraft;

    private boolean greeted;
    private Connection connection;

    public ClientLink(Outbox outbox, LongSupplier clock, String modVersion, String minecraft) {
        this.outbox = outbox;
        this.clock = clock;
        this.modVersion = modVersion;
        this.minecraft = minecraft;
    }

    /** The server said it reads the channel. The first time on a connection, that is answered with the hello. */
    public synchronized void serverListens(int guiScale, String locale) {
        if (greeted) {
            return;
        }
        greeted = true;
        outbox.send(ClientCodec.encode(new Hello(Wire.PROTOCOL, modVersion, minecraft,
                Arrays.stream(ClientFeature.values()).map(FeatureRange::of).toList(), guiScale, locale)));
    }

    public synchronized void received(byte[] frame) {
        try {
            ClientCodec.decodeClientbound(frame).ifPresent(packet -> {
                if (packet instanceof Welcome welcome) {
                    welcomed(welcome);
                }
            });
        } catch (MalformedPacketException notAPacket) {
            // A server is as free to send nonsense as a client is.
        }
    }

    public synchronized void disconnected() {
        greeted = false;
        connection = null;
    }

    public synchronized Optional<Connection> connection() {
        return Optional.ofNullable(connection);
    }

    private void welcomed(Welcome welcome) {
        if (!greeted || welcome.protocol() < 1 || welcome.protocol() > Wire.PROTOCOL) {
            return;
        }
        Map<ClientFeature, Integer> agreed = new EnumMap<>(ClientFeature.class);
        for (FeatureVersion each : welcome.agreed()) {
            ClientFeature.byId(each.feature())
                    .filter(known -> each.version() >= known.oldest() && each.version() <= known.newest())
                    .ifPresent(known -> agreed.put(known, each.version()));
        }
        if (agreed.containsKey(ClientFeature.SESSION)) {
            connection = new Connection(welcome.serverName(), welcome.theme(), agreed, clock.getAsLong());
        }
    }
}
