# AA Guard (LSPosed module)
Blocks Android Auto (com.google.android.projection.gearhead) from auto-connecting.
You must explicitly allow it via a Quick Settings tile or the launcher button.

## Build
- Open this folder in Android Studio (Giraffe+)
- Let it sync. Then build and install `app` to your rooted device.
- In LSPosed, enable **AA Guard** for package **com.google.android.projection.gearhead**. Reboot.
- Allow superuser in Magisk - this is to allow setting of global props and force refreshing bluetooth and wifi for Android Auto connection

## Use
- By default, AA will not autostart.
- When you want to connect, tap the QS tile **Allow AA** or launch the app **Launch Android Auto**.

## Notes:
Use at your own risk.  Not responsible for bricks, the apocalypse, fried vehicles etc.  
