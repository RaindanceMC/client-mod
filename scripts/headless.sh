#!/usr/bin/env bash
# Runs a command with a game client on a virtual screen, never on the desktop of whoever is sitting here.
# Xvfb alone is not enough: a client that finds WAYLAND_DISPLAY opens its window there instead.
set -euo pipefail
exec env -u WAYLAND_DISPLAY -u HYPRLAND_INSTANCE_SIGNATURE XDG_SESSION_TYPE=x11 \
	xvfb-run -a -s "-screen 0 1280x720x24" "$@"
