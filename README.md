# NodeApp — Android app that runs Node.js applications

This Android app runs Node.js applications on-device using the
**[nodejs-mobile](https://github.com/nodejs-mobile/nodejs-mobile)** framework
(“Node.js for Mobile Apps”).

The app embeds the prebuilt Node.js runtime (`libnode.so`) that the
nodejs-mobile project publishes for Android (`armeabi-v7a`, `arm64-v8a` and
`x86_64`), starts it through the Node.js C++ embedder API (`node::Start`) on a
background thread, and bridges the Java/Kotlin UI layer with the native runtime
via JNI.

## How it works

1. `MainActivity` loads two native libraries:
   - `native-lib` — the app’s own JNI glue (`app/src/main/cpp/native-lib.cpp`)
   - `node` — the prebuilt Node.js runtime from
     `app/src/main/jniLibs/<abi>/libnode.so` (imported into the CMake build via
     `add_library(libnode SHARED IMPORTED)`).
2. When the activity starts, the app copies the files bundled in
   `app/src/main/assets/` into the app’s writable internal storage
   (`/data/data/<package>/files/nodejs/`).
3. The app launches `node <script>` (`node::Start(argc, argv)`) with the copied
   script on a background thread.
4. stdout/stderr of the Node.js process are redirected into Android’s logcat, and
   the exit code of the process is reported back (`RESULT:PASS` / `RESULT:FAIL`).

## Project layout

```
nodejs-android-app/
├── build.gradle
├── settings.gradle
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       ├── gradle-wrapper.properties
│       ├── gradlew
│       └── gradlew.bat
└── app/
    ├── build.gradle
    ├── CMakeLists.txt
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── assets/
        │   │   └── main.js          # the default Node.js app that gets executed
        │   ├── cpp/
        │   │   ├── include/         # Node.js + V8 headers shipped by the nodejs-mobile release
        │   │   └── native-lib.cpp   # JNI glue -> node::Start
        │   ├── java/
        │   │   └── nodejsmobile/
        │   │       └── nodeapp/
        │   │           └── MainActivity.java
        │   ├── jniLibs/
        │   │   ├── arm64-v8a/
        │   │   │   └── libnode.so
        │   │   ├── armeabi-v7a/
        │   │   │   └── libnode.so
        │   │   └── x86_64/
        │   │       └── libnode.so
        │   └── res/
        │       └── layout/
        │           └── activity_main.xml
```

## Prerequisites

- [Android Studio](https://developer.android.com/studio) with JDK 8+
- Android SDK (platform for the `compileSdkVersion` used, e.g. platform 26)
- Android NDK `24.0.8215888` (this is the NDK version the prebuilt `libnode.so`
  binaries of nodejs-mobile were built with)

## Build

Open the project folder in Android Studio and press **Run**, or build from the
command line:

```sh
cd nodejs-android-app
./gradlew assembleDebug
```

The debug APK will be produced at `app/build/outputs/apk/debug/app-debug.apk`.

## Run

Install and launch the app on a device/emulator:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n nodejsmobile.nodeapp/.MainActivity
adb logcat -s NodeApp:V | grep -m 1 "^RESULT:"
```

The first launch copies the bundled assets to the app’s writable directory and
runs the default Node.js script. Output from the Node.js process is forwarded to
logcat (tag `NodeApp`).

## Running your own Node.js application

1. Put your server/application file(s) into `app/src/main/assets/` (for the
   default entry: `app/src/main/assets/main.js`).
2. Rebuild the APK (`./gradlew assembleDebug`).
3. Launch the app; it will copy the assets and execute the Node.js entry script.

Alternatively, you can pass a custom Node.js invocation as an intent extra:

```sh
adb shell am start -n nodejsmobile.nodeapp/.MainActivity \
    -e "nodeargs" "/data/data/nodejsmobile.nodeapp/files/nodejs/main.js --port 8080"
```

The `nodeargs` string is tokenized and handed to the Node.js runtime as
`node [flags] <script>` arguments.

## Notes and limitations

- Node.js runs on a single background thread inside the app process.
- The runtime is single-instance per process; multiple tasks should be modeled
  with `require` of the same modules from a single main script.
- Mobile OS sandboxing applies: Node.js code should only write into writable
  app paths (the copied assets directory or `getExternalFilesDir()`/`getCacheDir()`).
- `child_process.spawn()` / `fork()` are subject to Android runtime permissions.
- Native Node.js addons must be cross-compiled for Android (see the
  [nodejs-mobile native modules documentation](https://github.com/nodejs-mobile/nodejs-mobile#-native-modules)
  and the module compatibility repo).
