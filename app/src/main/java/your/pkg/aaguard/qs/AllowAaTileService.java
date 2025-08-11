package your.pkg.aaguard.qs;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import your.pkg.aaguard.AllowWindow;
import your.pkg.aaguard.R;

public class AllowAaTileService extends TileService {

    private static final String TAG = "AA-Guard-Tile";
    private static final String AA_PKG = "com.google.android.projection.gearhead";

    @Override
    public void onStartListening() {
        super.onStartListening();
        boolean allowed = AllowWindow.getMasterAllowApp(getApplicationContext());
        updateTileState(allowed);
    }

    @Override
    public void onClick() {
        // optional: RF cycle Wi-Fi & Bluetooth (runs in background thread)
        rfCycle();

        boolean current = AllowWindow.getMasterAllowApp(getApplicationContext());
        boolean next = !current;

        // Persist state + set system property (handled inside AllowWindow)
        AllowWindow.setMasterAllowApp(getApplicationContext(), next);

        // Update tile UI
        updateTileState(next);

        if (next) {
            // When enabling: clear any stale AA and launch it
            try {
                Log.d(TAG, "force-stop gearhead");
                Runtime.getRuntime().exec(new String[]{"su","-c","/system/bin/am force-stop " + AA_PKG}).waitFor();
            } catch (Throwable e) {
                Log.e(TAG, "force-stop failed", e);
            }
            try {
                PackageManager pm = getPackageManager();
                Intent launch = pm.getLaunchIntentForPackage(AA_PKG);
                if (launch != null) {
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityAndCollapse(launch);
                    Log.d(TAG, "launched AA via launcher intent");
                } else {
                    // explicit fallback to HeadunitActivity
                    Intent explicit = new Intent();
                    explicit.setClassName(AA_PKG, "com.google.android.projection.gearhead.HeadunitActivity");
                    explicit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityAndCollapse(explicit);
                    Log.d(TAG, "launched AA via explicit component");
                }
            } catch (Throwable e) {
                Log.e(TAG, "launch AA failed", e);
            }
        } else {
            // When disabling: just kill AA
            try {
                Log.d(TAG, "force-stop gearhead");
                Runtime.getRuntime().exec(new String[]{"su","-c","/system/bin/am force-stop " + AA_PKG}).waitFor();
            } catch (Throwable e) {
                Log.e(TAG, "force-stop failed", e);
            }
        }
    }

    private void updateTileState(boolean allowed) {
        Tile t = getQsTile();
        if (t == null) return;

        t.setState(allowed ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        t.setLabel(allowed ? "AA Allowed" : "AA Blocked");
        try {
            t.setIcon(Icon.createWithResource(
                    this,
                    allowed ? R.drawable.directions_car_24    // ON icon
                            : R.drawable.bluetooth_drive_24   // OFF icon
            ));
        } catch (Throwable ignored) {}
        t.updateTile();
    }

    /** Disable Wi-Fi & Bluetooth for ~5s, then re-enable (root required). */
    private void rfCycle() {
        new Thread(() -> {
            try {
                Log.d(TAG, "RF cycle start: disabling Wi-Fi & Bluetooth");
                // Wi-Fi off
                Runtime.getRuntime().exec(new String[]{"su","-c","/system/bin/svc wifi disable"}).waitFor();
                // Bluetooth off (try cmd, then fallback)
                int rc = Runtime.getRuntime().exec(new String[]{"su","-c","/system/bin/cmd bluetooth_manager disable"}).waitFor();
                if (rc != 0) {
                    Runtime.getRuntime().exec(new String[]{"su","-c","/system/bin/service call bluetooth_manager 8"}).waitFor();
                }

                Thread.sleep(5000);

                Log.d(TAG, "RF cycle end: enabling Wi-Fi & Bluetooth");
                // Wi-Fi on
                Runtime.getRuntime().exec(new String[]{"su","-c","/system/bin/svc wifi enable"}).waitFor();
                // Bluetooth on (try cmd, then fallback)
                rc = Runtime.getRuntime().exec(new String[]{"su","-c","/system/bin/cmd bluetooth_manager enable"}).waitFor();
                if (rc != 0) {
                    Runtime.getRuntime().exec(new String[]{"su","-c","/system/bin/service call bluetooth_manager 6"}).waitFor();
                }
            } catch (Throwable e) {
                Log.e(TAG, "RF cycle failed", e);
            }
        }).start();
    }
}
