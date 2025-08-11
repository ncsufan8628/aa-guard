package your.pkg.aaguard;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {
    private final String aaPkg = "com.google.android.projection.gearhead";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!aaPkg.equals(lpparam.packageName)) return;

        // 1) Block Services starting when not allowed
        try {
            XposedBridge.hookAllMethods(Service.class, "onStartCommand", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Service svc = (Service) param.thisObject;
                    if (svc == null) return;
                    boolean allowed = isMasterAllow();
                    String comp = svc.getClass().getName();
                    XposedBridge.log("AA-Guard: onStartCommand " + comp + " sysprop=" + allowed + (allowed ? " ALLOW" : " BLOCK"));
                    if (!allowed) {
                        try { svc.stopSelf(); } catch (Throwable ignored) {}
                        param.setResult(Service.START_NOT_STICKY);
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("AA-Guard: hook Service.onStartCommand failed: " + t);
        }

        // 2) Block binds when not allowed (some AA uses bound services)
        try {
            XposedBridge.hookAllMethods(Service.class, "onBind", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Service svc = (Service) param.thisObject;
                    if (svc == null) return;
                    boolean allowed = isMasterAllow();
                    String comp = svc.getClass().getName();
                    XposedBridge.log("AA-Guard: onBind " + comp + " sysprop=" + allowed + (allowed ? " ALLOW" : " BLOCK"));
                    if (!allowed) {
                        param.setResult(null);
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("AA-Guard: hook Service.onBind failed: " + t);
        }

        // 3) Block BroadcastReceiver entries if not allowed
        try {
            XposedBridge.hookAllMethods(BroadcastReceiver.class, "onReceive", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    BroadcastReceiver rcvr = (BroadcastReceiver) param.thisObject;
                    boolean allowed = isMasterAllow();
                    String comp = (rcvr != null) ? rcvr.getClass().getName() : "<null>";
                    XposedBridge.log("AA-Guard: onReceive " + comp + " sysprop=" + allowed + (allowed ? " ALLOW" : " BLOCK"));
                    if (!allowed) {
                        param.setResult(null);
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("AA-Guard: hook BroadcastReceiver.onReceive failed: " + t);
        }

        // 4) Kill HeadunitActivity onCreate if it slips through
        try {
            Class<?> headunit = Class.forName(
                "com.google.android.projection.gearhead.HeadunitActivity",
                false,
                lpparam.classLoader
            );
            XposedBridge.hookAllMethods(headunit, "onCreate", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Activity a = (Activity) param.thisObject;
                    boolean allowed = isMasterAllow();
                    String comp = (a != null) ? a.getClass().getName() : "HeadunitActivity";
                    XposedBridge.log("AA-Guard: onCreate " + comp + " sysprop=" + allowed + (allowed ? " ALLOW" : " BLOCK"));
                    if (!allowed) {
                        if (a != null) a.finish();
                        param.setResult(null);
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("AA-Guard: hook HeadunitActivity.onCreate failed (class may be obfuscated): " + t);
        }
    }

    private static boolean isMasterAllow() {
        try {
            Class<?> cls = Class.forName("android.os.SystemProperties");
            Method get = cls.getMethod("get", String.class, String.class);
            String v = (String) get.invoke(null, "sys.aaguard.allow", "0");
            boolean allowed = "1".equals(v) || "true".equalsIgnoreCase(v);
            XposedBridge.log("AA-Guard: sysprop sys.aaguard.allow=" + v + " -> " + allowed);
            return allowed;
        } catch (Throwable t) {
            XposedBridge.log("AA-Guard: sysprop read failed: " + t);
            return false;
        }
    }
}
