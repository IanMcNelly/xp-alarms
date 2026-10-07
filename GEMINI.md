# RuneLite Plugin Development & Plugin Hub Guidelines

This document contains critical constraints, API rules, and lessons learned for developing RuneLite plugins and submitting them to the official RuneLite Plugin Hub.

---

## 1. Package Namespace Restrictions
- **Rule**: Custom plugin code must **never** be placed inside the `net.runelite.*` package namespace.
- **Why**: The Plugin Hub build pipeline flags this with `net.runelite.pluginhub.packager.PluginBuildException: use of net.runelite package namespace is not allowed`.
- **Solution**: Always place plugin classes under your own custom package namespace (e.g., `com.<author>.<plugin>` or `com.gullesurgames.<plugin>`).

---

## 2. Gson & JSON Serialization
- **Rule**: Do **not** instantiate fresh Gson objects (`new Gson()` or `new GsonBuilder()`).
- **Why**: RuneLite flags this as a terminally deprecated API violation during hub packaging: `Do not create fresh Gson instances, always @Inject the client's Gson.`
- **Solution**:
  - Always inject the client's `Gson` instance:
    ```java
    @Inject
    private Gson gson;
    ```
  - If custom type adapters or formatting are needed, derive a new builder from the injected client instance:
    ```java
    this.gson = clientGson.newBuilder()
        .registerTypeAdapter(Color.class, new ColorAdapter())
        .create();
    ```

---

## 3. Audio Playback
- **Rule**: Do **not** use `javax.sound.*` APIs directly (e.g., `AudioSystem`, `Clip`, `AudioInputStream`, `LineListener`, `DataLine`).
- **Why**: Plugin Hub automated review scanners flag direct Java sound APIs: `Use of javax.sound, use net.runelite.client.audio.AudioPlayer instead`.
- **Solution**:
  - Inject or instantiate `net.runelite.client.audio.AudioPlayer`:
    ```java
    @Inject
    private AudioPlayer audioPlayer;
    ```
  - Use `audioPlayer.play(Filepath, float)` or other supported overloads for audio playback.

---

## 4. File I/O & Plugin Directory Storage
- **Rule**: All file I/O must be performed using `net.runelite.client.util.Filepath`, **not** direct Java APIs (`java.io.File`, `java.io.FileInputStream`, `java.nio.file.Path`, etc.).
- **Requirements**:
  1. Set `internalName` in `@PluginDescriptor` to **match exactly** the plugin name in the Plugin Hub repository (`plugins/<plugin-name>`) (and optionally `legacyDataDirectory` for backward-compatible migration from `.runelite/<name>`):
     ```java
     @PluginDescriptor(
         name = "XP Alarm",
         description = "...",
         internalName = "xp-alarms", // MUST match the filename in plugin-hub/plugins/<name>
         legacyDataDirectory = "xpalarm"
     )
     ```
  2. Access the plugin's data directory inside `Plugin` subclasses via:
     ```java
     Filepath pluginDir = getPluginDirectory(); // resolves to .runelite/plugin-data/<internalName>
     Filepath customSubDir = pluginDir.join("subfolder");
     ```
  3. Use `Filepath` methods for operations:
     - `createDirectories()`, `createDirectory()`
     - `exists()`, `isFile()`, `isDirectory()`
     - `join(String segment)`
     - `walk(int maxDepth)` (always use try-with-resources: `try (Stream<Filepath> stream = dir.walk(1)) { ... }`)
     - `openInputStream()`, `openOutputStream()`, `write(byte[])`, `write(String)`
  4. **Security Notice**: Never call `Filepath.Unchecked.*` in production plugin code (`src/main`). Plugin Hub security scanners will reject PRs using `Filepath.Unchecked`.

---

## 5. Plugin Hub Pull Requests
- In the `runelite/plugin-hub` repository (or your fork), each plugin entry resides at `plugins/<plugin-name>` and contains:
  ```properties
  repository=https://github.com/<user>/<repo>.git
  commit=<full-40-char-git-commit-hash>
  ```
- Any time changes are pushed to your plugin repository, update the commit hash in `plugins/<plugin-name>` on your hub fork branch and push to trigger automated GitHub Actions verification.
