package your.pkg.aaguard;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** Harmless legacy LSPosed entry point retained for upgrades from version 1. */
public final class HookEntry implements IXposedHookLoadPackage {
    private static final String ANDROID_AUTO_PACKAGE = "com.google.android.projection.gearhead";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (ANDROID_AUTO_PACKAGE.equals(lpparam.packageName)) {
            XposedBridge.log("AA-Guard: v2 loaded; unsafe lifecycle hooks are disabled");
        }
    }
}
