#!/bin/bash

set -x

DIRNAME="$(dirname $0)"

cd "$DIRNAME/.."

bb uberjar target/pod-ilmoraunio-cljconf.jar -m pod-ilmoraunio-cljconf.core

[ ! -f bb ] && curl -sLO https://github.com/babashka/babashka/releases/download/v1.3.191/babashka-1.3.191-macos-aarch64.tar.gz && tar xzvf babashka-1.3.191-macos-aarch64.tar.gz

cat bb target/pod-ilmoraunio-cljconf.jar > pod-ilmoraunio-cljconf

chmod +x pod-ilmoraunio-cljconf
