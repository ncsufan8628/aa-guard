package your.pkg.aaguard;

/** Constant, non-user-controlled root commands used by the guard. */
final class GuardCommands {
    private GuardCommands() {}

    static String enablePackage() {
        return "/system/bin/pm enable --user 0 " + GuardController.ANDROID_AUTO_PACKAGE;
    }

    static String disablePackage() {
        return "/system/bin/pm disable-user --user 0 " + GuardController.ANDROID_AUTO_PACKAGE;
    }

    static String forceStop() {
        return "/system/bin/am force-stop " + GuardController.ANDROID_AUTO_PACKAGE;
    }
}
