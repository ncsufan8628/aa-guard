package your.pkg.aaguard.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import your.pkg.aaguard.AllowWindow;

public class LauncherActivity extends Activity {
    private static final String TAG = "AA-Guard-Launcher";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set master allow ON (pure allow)
        AllowWindow.setMasterAllowApp(getApplicationContext(), true);
        try { Runtime.getRuntime().exec(new String[]{ "su","-c","/system/bin/setprop sys.aaguard.allow 1" }).waitFor(); } catch (Throwable ignored) {}
        android.util.Log.d(TAG, "Set master_allow=true");

        // If rooted, clear any stale AA state
        try { android.util.Log.d(TAG, "force-stop gearhead"); Runtime.getRuntime().exec(new String[]{ "su", "-c", "/system/bin/am force-stop com.google.android.projection.gearhead" }); } catch (Throwable e) { android.util.Log.e(TAG, "force-stop failed", e);}

        // Launch Android Auto - prefer launcher intent, fallback to HeadunitActivity
        try {
            PackageManager pm = getPackageManager();
            Intent launch = pm.getLaunchIntentForPackage("com.google.android.projection.gearhead");
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launch);
            android.util.Log.d(TAG, "launched AA via launcher intent");
            } else {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(
                    "com.google.android.projection.gearhead",
                    "com.google.android.projection.gearhead.HeadunitActivity"
                ));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                android.util.Log.d(TAG, "launched AA via explicit component");
            }
        } catch (Throwable ignored) {}

        finish();
    }
}
