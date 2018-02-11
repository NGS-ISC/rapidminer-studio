.PHONY: build run clean all idea ide upgrade

all:
	./gradlew tasks

build:
	./gradlew jar

clean:
	./gradlew clean

run:
	./gradlew run

idea:
	./gradlew idea

ide: idea

upgrade:
	./gradlew build --refresh-dependencies
