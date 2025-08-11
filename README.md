# AA Guard (LSPosed module)
Blocks Android Auto (com.google.android.projection.gearhead) from auto-connecting.
You must explicitly allow it via a Quick Settings tile or the launcher button.

## Build
- Open this folder in Android Studio (Giraffe+)
- Let it sync. Then build and install `app` to your rooted device.
- In LSPosed, enable **AA Guard** for package **com.google.android.projection.gearhead**. Reboot.

## Use
- By default, AA will not autostart.
- When you want to connect, tap the QS tile **Allow AA** (gives 2 minutes) or launch the app **Launch Android Auto**.

## Notes
- Tested minSdk 24+, targetSdk 34.
- You can change the allow window in `AllowWindow.DURATION_MS`.
