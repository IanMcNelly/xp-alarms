---
name: runelite-plugin-dev
description: >-
  Provides best practices, API constraints, and submission workflows for developing
  RuneLite plugins and submitting them to the official RuneLite Plugin Hub.
  Use when developing, testing, packaging, or debugging RuneLite plugins.
---

# RuneLite Plugin Development & Plugin Hub Runbook

## Pre-Submission Verification Checklist
1. [ ] **Package Namespace**: All plugin classes reside in an external package namespace (never `net.runelite.*`).
2. [ ] **Gson Injection**: `@Inject private Gson gson;` used instead of `new Gson()` or `new GsonBuilder()`. Use `gson.newBuilder()` for custom configuration.
3. [ ] **Audio Subsystem**: `net.runelite.client.audio.AudioPlayer` used instead of `javax.sound.*` APIs.
4. [ ] **File I/O**: `net.runelite.client.util.Filepath` used for directory/file operations retrieved via `Plugin.getPluginDirectory()`. No `Filepath.Unchecked` in production code (`src/main`).
5. [ ] **Descriptor Alignment**: `@PluginDescriptor.internalName` exactly matches the plugin entry filename in `runelite/plugin-hub/plugins/<plugin-name>`.
6. [ ] **Automated Tests**: All unit tests pass locally via `./gradlew clean test build`.

## Hub PR Update & CI Workflow
1. Commit and push all plugin source changes to the plugin repository's `main` branch.
2. Obtain the full 40-character commit hash:
   ```bash
   git rev-parse HEAD
   ```
3. In your local `plugin-hub` fork (`plugins/<plugin-name>`), update the `commit` property:
   ```properties
   repository=https://github.com/<user>/<repo>.git
   commit=<full-40-character-hash>
   ```
4. Commit and push the updated descriptor to your PR branch:
   ```bash
   git commit -am "<plugin-name>: update commit to <hash>"
   git push origin <pr-branch>
   ```
5. Monitor GitHub Actions CI status using the GitHub CLI:
   ```bash
   gh pr checks <pr-number> --repo runelite/plugin-hub
   ```
