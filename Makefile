
# --------------------------------------------------------------------
# Author: Menuka Warushavithana
# --------------------------------------------------------------------

.PHONY: build
build:
	chmod +x gradlew
	./gradlew install

.PHONY: run-census-server
run-census-server:
	sh ./build/install/sustain-census-grpc/bin/census-server


.PHONY: run-census-client
run-census-client:
	sh ./build/install/sustain-census-grpc/bin/census-client


proto:
	./gradlew generateProto


clean:
	rm -rf build