package your.pkg.aaguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/** Applies guard state without interfering with Android Auto lifecycle callbacks. */
public final class GuardController {
    public interface Callback {
        void onComplete(boolean success, boolean allowed, String detail);
    }

    public static final String ANDROID_AUTO_PACKAGE = "com.google.android.projection.gearhead";
    private static final String TAG = "AA-Guard";
    private static final String PREF_ALLOWED = "master_allow";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final AtomicBoolean BUSY = new AtomicBoolean(false);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private GuardController() {}

    public static boolean isAllowed(Context context) {
        return preferences(context).getBoolean(PREF_ALLOWED, false);
    }

    public static boolean isBusy() {
        return BUSY.get();
    }

    public static void setAllowed(Context context, boolean allowed, boolean cycleRadios,
                                  Callback callback) {
        Context app = context.getApplicationContext();
        if (!BUSY.compareAndSet(false, true)) {
            deliver(callback, false, isAllowed(app), "Another guard operation is still running");
            return;
        }

        EXECUTOR.execute(() -> {
            CommandResult result;
            try {
                result = allowed ? enableAndroidAuto(cycleRadios) : disableAndroidAuto();
                if (result.success) {
                    preferences(app).edit().putBoolean(PREF_ALLOWED, allowed).apply();
                }
            } catch (Throwable t) {
                Log.e(TAG, "Unable to change guard state", t);
                result = new CommandResult(false, t.toString());
            } finally {
                BUSY.set(false);
            }
            deliver(callback, result.success, isAllowed(app), result.output);
        });
    }

    /** Always return to blocked state after a reboot. */
    public static void blockAfterBoot(Context context) {
        Context app = context.getApplicationContext();
        EXECUTOR.execute(() -> {
            CommandResult result = disableAndroidAuto();
            if (result.success) {
                preferences(app).edit().putBoolean(PREF_ALLOWED, false).apply();
            }
            Log.i(TAG, "Boot guard applied: " + result.success + " " + result.output);
        });
    }

    private static CommandResult enableAndroidAuto(boolean cycleRadios) {
        CommandResult enable = runRoot(GuardCommands.enablePackage());
        if (!enable.success) return enable;
        CommandResult stop = runRoot(GuardCommands.forceStop());
        if (!stop.success) return stop;
        if (cycleRadios) {
            CommandResult cycle = cycleEnabledRadios();
            if (!cycle.success) return cycle;
        }
        return new CommandResult(true, "Android Auto enabled");
    }

    private static CommandResult disableAndroidAuto() {
        CommandResult stop = runRoot(GuardCommands.forceStop());
        if (!stop.success) return stop;
        CommandResult disable = runRoot(GuardCommands.disablePackage());
        return disable.success ? new CommandResult(true, "Android Auto blocked") : disable;
    }

    private static CommandResult cycleEnabledRadios() {
        boolean wifiWasOn = "1".equals(runRoot("/system/bin/settings get global wifi_on").output.trim());
        boolean bluetoothWasOn = "1".equals(runRoot("/system/bin/settings get global bluetooth_on").output.trim());
        boolean wifiDisabled = false;
        boolean bluetoothDisabled = false;
        String failure = null;
        try {
            if (wifiWasOn) {
                CommandResult result = runRoot("/system/bin/svc wifi disable");
                wifiDisabled = result.success;
                if (!result.success) failure = result.output;
            }
            if (failure == null && bluetoothWasOn) {
                CommandResult result = runRoot("/system/bin/cmd bluetooth_manager disable");
                bluetoothDisabled = result.success;
                if (!result.success) failure = result.output;
            }
            if (failure == null && (wifiDisabled || bluetoothDisabled)) Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failure = "Radio cycle interrupted";
        } finally {
            if (wifiDisabled) {
                CommandResult restore = runRoot("/system/bin/svc wifi enable");
                if (!restore.success && failure == null) failure = "Could not restore Wi-Fi: " + restore.output;
            }
            if (bluetoothDisabled) {
                CommandResult restore = runRoot("/system/bin/cmd bluetooth_manager enable");
                if (!restore.success && failure == null) failure = "Could not restore Bluetooth: " + restore.output;
            }
        }
        return failure == null ? new CommandResult(true, "Radios cycled") : new CommandResult(false, failure);
    }

    private static CommandResult runRoot(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = new ProcessBuilder("su", "-c", command).redirectErrorStream(true).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (output.length() > 0) output.append('\n');
                    output.append(line);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0 && output.length() == 0) output.append("exit code ").append(exitCode);
            return new CommandResult(exitCode == 0, output.toString());
        } catch (Throwable t) {
            return new CommandResult(false, t.toString());
        }
    }

    private static SharedPreferences preferences(Context context) {
        Context storage = context.createDeviceProtectedStorageContext();
        return storage.getSharedPreferences(BuildConfig.PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static void deliver(Callback callback, boolean success, boolean allowed, String detail) {
        if (callback != null) MAIN.post(() -> callback.onComplete(success, allowed, detail));
    }

    private static final class CommandResult {
        final boolean success;
        final String output;

        CommandResult(boolean success, String output) {
            this.success = success;
            this.output = output == null ? "" : output;
        }
    }
}
