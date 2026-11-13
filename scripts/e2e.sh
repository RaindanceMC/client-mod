#!/usr/bin/env bash
# The whole way once: a real Paper with RainsCore, and a real client of each version that has to be welcomed
# by it. The older clients come in through ViaVersion + ViaBackwards, which is how they reach hyadesmc.net.
#
#   scripts/e2e.sh <paper.jar> <RainsCore.jar> [more plugin jars…] -- [26.3 26.2 1.21.11]
#
# The server gets its own temporary folder and is stopped at the end. The clients run on a virtual screen.
set -euo pipefail

paper="$1"; core="$2"; shift 2
plugins=("$core")
while [ $# -gt 0 ] && [ "$1" != "--" ]; do plugins+=("$1"); shift; done
[ $# -gt 0 ] && shift
versions=("$@"); [ ${#versions[@]} -gt 0 ] || versions=(26.3 26.2 1.21.11)

here="$(cd "$(dirname "$0")/.." && pwd)"
folder="$(mktemp -d -t hyades-e2e-XXXXXX)"
port=25610
mkdir -p "$folder/plugins"
cp "$paper" "$folder/paper.jar"
cp "${plugins[@]}" "$folder/plugins/"
printf 'eula=true\n' > "$folder/eula.txt"
cat > "$folder/server.properties" <<PROPS
online-mode=false
enforce-secure-profile=false
server-port=$port
server-ip=127.0.0.1
motd=Hyades E2E
level-type=minecraft\:flat
view-distance=4
simulation-distance=4
white-list=false
PROPS

(cd "$folder" && exec java -Xmx2G -Dcom.mojang.eula.agree=true -jar paper.jar --nogui > "$folder/server.log" 2>&1) &
server=$!
trap 'kill $server 2>/dev/null || true' EXIT

for _ in $(seq 1 120); do
	grep -q 'Done (' "$folder/server.log" 2>/dev/null && break
	kill -0 $server 2>/dev/null || { echo "the server died while starting, see $folder/server.log"; exit 1; }
	sleep 2
done

failed=0
for version in "${versions[@]}"; do
	if "$here/scripts/headless.sh" "$here/gradlew" --no-daemon -p "$here/versions/fabric-$version" runClientGameTest \
			-Phyades.e2e.server=127.0.0.1:$port "-Phyades.e2e.name=Hyades E2E" > "$folder/client-$version.log" 2>&1; then
		echo "HYADES-E2E $version: WELCOMED"
	else
		echo "HYADES-E2E $version: FAILED, see $folder/client-$version.log"
		failed=1
	fi
done
exit $failed
