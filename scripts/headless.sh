#!/usr/bin/env bash
# Runs a command with a game client on a virtual screen, never on the desktop of whoever is sitting here.
# Xvfb alone is not enough: a client that finds WAYLAND_DISPLAY opens its window there instead.
#
# OpenGL is Mesa's software renderer — what a CI runner without a GPU has, and the same picture everywhere.
# Since 26.3 the client opens its window through SDL. Left alone it asks the virtual screen's GLX for a visual
# it does not have ("Couldn't find matching GLX visual") and, where there is no Vulkan to fall back to, never
# gets a window at all — so SDL is told to use EGL, and EGL to be Mesa's rather than a GPU driver's.
set -euo pipefail
exec env -u WAYLAND_DISPLAY -u HYPRLAND_INSTANCE_SIGNATURE XDG_SESSION_TYPE=x11 \
	LIBGL_ALWAYS_SOFTWARE=1 GALLIUM_DRIVER=llvmpipe __GLX_VENDOR_LIBRARY_NAME=mesa \
	__EGL_VENDOR_LIBRARY_FILENAMES=/usr/share/glvnd/egl_vendor.d/50_mesa.json \
	SDL_VIDEODRIVER=x11 SDL_VIDEO_FORCE_EGL=1 SDL_VIDEO_X11_FORCE_EGL=1 \
	xvfb-run -a -s "-screen 0 1280x720x24" "$@"
