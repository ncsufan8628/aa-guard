package your.pkg.aaguard.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import your.pkg.aaguard.GuardController;

/** Requires explicit confirmation so another app cannot silently enable AA. */
public final class LauncherActivity extends Activity {
    private static final String TAG = "AA-Guard-Launcher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (GuardController.isAllowed(this)) {
            openSettingsAndFinish();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Allow Android Auto?")
                .setMessage("Android Auto will remain enabled until you block it from the Quick Settings tile or reboot.")
                .setPositiveButton("Allow", (dialog, which) -> enable())
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> finish())
                .setOnCancelListener(dialog -> finish())
                .show();
    }

    private void enable() {
        GuardController.setAllowed(this, true, true, (success, allowed, detail) -> {
            if (success) {
                openSettingsAndFinish();
            } else {
                Log.e(TAG, detail);
                Toast.makeText(this, "AA Guard failed: " + detail, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void openSettingsAndFinish() {
        try {
            Intent intent = new Intent("com.google.android.projection.gearhead.SETTINGS")
                    .setPackage(GuardController.ANDROID_AUTO_PACKAGE);
            startActivity(intent);
        } catch (Throwable e) {
            Log.w(TAG, "Android Auto settings activity is unavailable", e);
        }
        finish();
    }
}
