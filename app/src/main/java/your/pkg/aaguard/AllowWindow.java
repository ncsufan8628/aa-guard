package your.pkg.aaguard;

import android.content.Context;
import android.content.SharedPreferences;

public class AllowWindow {

    /** Toggle master_allow in app prefs and mirror to a system property (root). */
    public static void setMasterAllowApp(Context appCtx, boolean allowed) {
        // Persist for UI state
        SharedPreferences sp = appCtx.getSharedPreferences(BuildConfig.PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean("master_allow", allowed).apply();

        // Source of truth for the hook: system property
        try {
            String val = allowed ? "1" : "0";
            Runtime.getRuntime().exec(new String[]{ "su", "-c", "/system/bin/setprop sys.aaguard.allow " + val }).waitFor();
        } catch (Throwable ignored) {}
    }

    /** Read UI state (not used by the hook). */
    public static boolean getMasterAllowApp(Context appCtx) {
        SharedPreferences sp = appCtx.getSharedPreferences(BuildConfig.PREFS_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean("master_allow", false);
    }
}
