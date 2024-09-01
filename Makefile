BUILD_FILES=babashka-1.3.191-macos-aarch64.tar.gz target target/pod-conftest-clj.jar pod-conftest-clj

build-macos-aarch64:
	./scripts/build_macos.sh

clean:
	rm -rf $(BUILD_FILES)
