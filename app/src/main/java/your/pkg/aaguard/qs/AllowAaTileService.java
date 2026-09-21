package your.pkg.aaguard.qs;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;

import your.pkg.aaguard.GuardController;
import your.pkg.aaguard.R;

public final class AllowAaTileService extends TileService {
    private static final String TAG = "AA-Guard-Tile";

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileState(GuardController.isAllowed(this), GuardController.isBusy());
    }

    @Override
    public void onClick() {
        if (GuardController.isBusy()) return;
        boolean next = !GuardController.isAllowed(this);
        updateTileState(!next, true);
        GuardController.setAllowed(this, next, next, (success, allowed, detail) -> {
            updateTileState(allowed, false);
            if (!success) {
                Log.e(TAG, detail);
                Toast.makeText(this, "AA Guard failed: " + detail, Toast.LENGTH_LONG).show();
            } else if (allowed) {
                openAndroidAutoSettings();
            }
        });
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    private void openAndroidAutoSettings() {
        try {
            Intent intent = new Intent("com.google.android.projection.gearhead.SETTINGS")
                    .setPackage(GuardController.ANDROID_AUTO_PACKAGE)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                startActivityAndCollapse(pendingIntent);
            } else {
                startActivityAndCollapse(intent);
            }
        } catch (Throwable e) {
            Log.w(TAG, "Android Auto settings activity is unavailable", e);
        }
    }

    private void updateTileState(boolean allowed, boolean busy) {
        Tile tile = getQsTile();
        if (tile == null) return;
        tile.setState(busy ? Tile.STATE_UNAVAILABLE : allowed ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel(busy ? "AA Guard working…" : allowed ? "AA Allowed" : "AA Blocked");
        tile.setIcon(Icon.createWithResource(this,
                allowed ? R.drawable.directions_car_24 : R.drawable.bluetooth_drive_24));
        tile.updateTile();
    }
}
