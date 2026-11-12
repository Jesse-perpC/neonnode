# NeonNode ⚡

**The future of JavaScript runs on Android.**

NeonNode is a futuristic Android app that runs Node.js applications on-device via the [nodejs-mobile](https://github.com/nodejs-mobile/nodejs-mobile) framework — "Node.js for Mobile Apps".

![futuristic neon ui](https://img.shields.io/badge/UI-Neon%20Cyberpunk-00E5FF)

## ✨ Features

- **Full Node.js runtime embedded** — runs on-device via the prebuilt `libnode.so` from the nodejs-mobile releases (Node.js 18.20.4, NDK r24).
- **Neon cyberpunk UI** — glowing neon title, ambient neon glow, monospace typography and a RUN button that screams the future.
- **One-tap execution** — copies bundled assets into writable storage and launches the Node.js entry script.
- **Live status** — the UI tracks the run (`STATUS: AWAITING RUN` → `STATUS: NODE INITIALIZING...` → `STATUS: RUN COMPLETE // RESULT:PASS|FAIL`).
- **Custom runs** — pass `nodeargs` intent extra for flags/scripts, e.g. `--port 8080`.

## 🚀 Build

```sh
cd /path/to/neonnode
./gradlew assembleDebug
```

Requires: Android Studio, JDK 8+, Android SDK, NDK `24.0.8215888`.

## 📦 Release APK

Releases are automated via GitHub Actions: pushing a tag like `v2.0.0` builds the release APK (`./gradlew assembleRelease`) and uploads it as an artifact. The version name/code is derived from the tag, so each new tag produces an ascending version.

```sh
git tag v2.0.1
git push origin v2.0.1
```

## 📄 License

MIT — see [LICENSE](LICENSE). The embedded Node.js runtime is provided under the Node.js license by the [nodejs-mobile](https://github.com/nodejs-mobile/nodejs-mobile) project.

## 🔗 Useful links

- [nodejs-mobile](https://github.com/nodejs-mobile/nodejs-mobile) — Node.js for Mobile Apps
- [nodejs-mobile releases](https://github.com/nodejs-mobile/nodejs-mobile/releases) — prebuilt Android binaries
- [FAQ](https://github.com/nodejs-mobile/nodejs-mobile/blob/main/doc_mobile/FAQ.md) — mobile runtime caveats
