# Dungeon Now Loading — Fabric 26.3 port

Port of [Dungeon Now Loading](https://github.com/Hanarion/DungeonNowLoading-1.21.1-Port) (1.21.1 multiloader) to **Minecraft 26.3 / Fabric**.

This branch is independent of the `aseprite-builder` code on `main` (it is an orphan branch).

## Building

Requires JDK 25.

```
./gradlew build
```

The mod jar is written to `build/libs/`.

## Dependencies

- Fabric Loader 0.19.5+, Fabric API 0.161.0+26.3
- Cardinal Components API 8.1.0 and Forge Config API Port 26.3.1 (bundled via jar-in-jar)
