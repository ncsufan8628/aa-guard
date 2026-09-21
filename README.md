# AA Guard

AA Guard prevents Android Auto (`com.google.android.projection.gearhead`) from
starting until you explicitly allow it. It is intended for rooted Android
devices with Magisk. Version 2 no longer intercepts Android Auto lifecycle
methods: those hooks caused foreground-service crashes on current releases.

## Build

1. Install Android SDK 34 and JDK 17.
2. Create `local.properties` with your local `sdk.dir`, or set `ANDROID_HOME`.
3. Run `./gradlew assembleDebug`.
4. Install `app/build/outputs/apk/debug/app-debug.apk` and grant its Magisk
   superuser request.

The legacy LSPosed entry point is harmless and retained for upgrades. LSPosed
is no longer required for blocking.

## Use

- Add the **Allow AA** Quick Settings tile.
- Tap it once to enable Android Auto. AA Guard safely cycles radios that were
  already enabled, then opens the current Android Auto settings activity.
- Tap it again to force-stop and block Android Auto.
- Opening **Allow Android Auto** from the launcher requires confirmation.
- Every reboot returns to the blocked state.

AA Guard disables the Android Auto package for user 0 while blocked. Before
uninstalling AA Guard, use the tile to leave Android Auto allowed. If AA Guard
was removed while blocked, restore Android Auto from a root shell with:

```sh
pm enable --user 0 com.google.android.projection.gearhead
```

## Diagnostics

Failures from root commands are displayed to the user and logged under the
`AA-Guard` and `AA-Guard-Tile` tags. Source state is only updated after the
package-manager operation succeeds.

Use at your own risk. Root package and radio controls vary between Android
vendors and releases.
