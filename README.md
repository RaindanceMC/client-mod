# RaindanceMC/client-mod — Hyades

The client side of [hyadesmc.net](https://hyadesmc.net): a Fabric mod that draws what the server's plugins
otherwise fake with sidebars, action bars and particles. It is never needed to play, says nothing to a server
that did not ask for it, and is only ever sent what a player without it could see too.

Works with any server running [RainsCore](https://github.com/RaindanceMC/RainsCore) 2.90.0 or newer; the server
sends its own name and colours.

| Game | File | Java |
|---|---|---|
| 26.3 | `hyades-mc26.3-<version>.jar` | 25 |
| 26.2 | `hyades-mc26.2-<version>.jar` | 25 |
| 1.21.11 | `hyades-mc1.21.11-<version>.jar` | 21 |

Needs Fabric Loader and Fabric API. Older clients reach a 26.3 server through ViaVersion + ViaBackwards on
the server; the mod's channel passes through untouched.

## What it does today (0.1.0)

The handshake, and one plate that says so: joining a server that speaks to the mod shows its name and
"Client mod connected" for four seconds in the top left. Everything else builds on this.

## Layout

- `common/` — everything that does not touch a Minecraft class: the handshake (`link.ClientLink`) and every
  layout and animation, drawn on `ui.Canvas`. Tested without a game.
- `versions/fabric-<game>/` — one thin adapter per game version: the channel, where things are drawn, and
  `Canvas` on that version's GUI. 26.2 compiles the 26.3 sources.
- The wire format is [client-protocol](https://github.com/RaindanceMC/client-protocol), bundled in the jar.

## Build and test

```
./gradlew build                                              # unit tests + all three jars
scripts/headless.sh ./gradlew -p versions/fabric-26.3 runClientGameTest   # a real client, on a virtual screen
scripts/e2e.sh paper.jar RainsCore.jar ViaVersion.jar ViaBackwards.jar    # every version joins a real server
```

Always start a client through `scripts/headless.sh`: without it the game opens on the desktop.

Needs JDK 25 and read access to `packages.tstieh.de` (`packagesUsername` / `packagesPassword` in
`~/.gradle/gradle.properties`, or `REPOSILITE_USERNAME` / `REPOSILITE_PASSWORD`), or the protocol built
locally with `mvn install`.
