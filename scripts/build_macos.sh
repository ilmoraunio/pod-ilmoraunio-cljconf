#!/bin/bash

set -x

DIRNAME="$(dirname $0)"

cd "$DIRNAME/.."

bb uberjar target/pod-ilmoraunio-conftest-clj.jar -m pod-ilmoraunio-conftest-clj.core

[ ! -f bb ] && curl -sLO https://github.com/babashka/babashka/releases/download/v1.3.191/babashka-1.3.191-macos-aarch64.tar.gz && tar xzvf babashka-1.3.191-macos-aarch64.tar.gz

cat bb target/pod-ilmoraunio-conftest-clj.jar > pod-ilmoraunio-conftest-clj

chmod +x pod-ilmoraunio-conftest-clj
