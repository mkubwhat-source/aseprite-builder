#!/usr/bin/env bash
set -x
curl -s https://meta.fabricmc.net/v2/versions/game | head -c 1500; echo
curl -s https://meta.fabricmc.net/v2/versions/loader | head -c 600; echo
curl -s https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml | grep -o '<version>[^<]*26\.[^<]*</version>' | tail -30
curl -s https://maven.fabricmc.net/net/fabricmc/fabric-loom/maven-metadata.xml | tail -30
curl -s https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml | tail -15
curl -s https://maven.fabricmc.net/net/fabricmc/fabric-loom-remap/maven-metadata.xml | tail -15
curl -s 'https://api.modrinth.com/v2/project/forge-config-api-port/version?loaders=%5B%22fabric%22%5D' | python3 -c "import json,sys;[print(v['version_number'],v['game_versions']) for v in json.load(sys.stdin)[:8]]"
curl -s 'https://api.modrinth.com/v2/project/cardinal-components-api/version' | python3 -c "import json,sys;[print(v['version_number'],v['game_versions']) for v in json.load(sys.stdin)[:8]]"
curl -s https://maven.ladysnake.org/releases/org/ladysnake/cardinal-components-api/cardinal-components-base/maven-metadata.xml | tail -12
curl -s https://maven.fuzs.xyz/releases/fuzs/forgeconfigapiport/forgeconfigapiport-fabric/maven-metadata.xml | tail -12
curl -s https://piston-meta.mojang.com/mc/game/version_manifest_v2.json | python3 -c "import json,sys;d=json.load(sys.stdin);print(d['latest']);print([v['id'] for v in d['versions'][:30]])"
curl -s https://raw.githubusercontent.com/FabricMC/fabric-example-mod/HEAD/build.gradle
curl -s https://raw.githubusercontent.com/FabricMC/fabric-example-mod/HEAD/gradle.properties
curl -s https://raw.githubusercontent.com/FabricMC/fabric-example-mod/HEAD/settings.gradle
curl -s https://raw.githubusercontent.com/FabricMC/fabric-example-mod/HEAD/gradle/wrapper/gradle-wrapper.properties
curl -s https://raw.githubusercontent.com/FabricMC/fabric-example-mod/HEAD/src/main/resources/fabric.mod.json
