#!/usr/bin/env bash
# Builds the Compose Multiplatform examples for the browser and copies them to static/examples/<name>/,
# so that the website serves them at /examples/<name>/index.html (see docs/showcase).
set -euo pipefail
cd "$(dirname "$0")/../.."

examples=(blocksworld thermostat tictactoe vacuum-world)

./gradlew $(printf ':examples:%s:jsBrowserDistribution ' "${examples[@]}")

for example in "${examples[@]}"; do
  target="website/static/examples/$example"
  rm -rf "$target"
  mkdir -p "$target"
  cp -a "examples/$example/build/dist/js/productionExecutable/." "$target/"
  # Source maps, and skiko files that webpack already bundled into <example>.js and the hashed .wasm
  find "$target" \( -name '*.map' -o -name '*.mjs' -o -name 'skiko.wasm' \) -delete
done
