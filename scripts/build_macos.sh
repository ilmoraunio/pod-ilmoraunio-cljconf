#!/bin/bash

set -x

DIRNAME="$(dirname $0)"

cd "$DIRNAME/.."

bb uberjar target/pod-conftest-clj.jar -m pod-conftest-clj.core

mkdir -p target/
cd target/

[ ! -f bb ] && curl -sLO https://github.com/babashka/babashka/releases/download/v1.3.191/babashka-1.3.191-macos-aarch64.tar.gz && tar xzvf babashka-1.3.191-macos-aarch64.tar.gz

cat bb pod-conftest-clj.jar > pod-conftest-clj

chmod +x pod-conftest-clj
