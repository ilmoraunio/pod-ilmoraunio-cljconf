BUILD_FILES=babashka-1.3.191-macos-aarch64.tar.gz target target/pod-conftest-clj.jar pod-conftest-clj

pod-conftest-parser/pod-conftest-parser:
	@make -C pod-conftest-parser

build-macos-aarch64: pod-conftest-parser/pod-conftest-parser
	./scripts/build_macos.sh

clean:
	rm -rf $(BUILD_FILES)
