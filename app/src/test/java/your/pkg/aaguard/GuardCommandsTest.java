package your.pkg.aaguard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class GuardCommandsTest {
    private static final String PACKAGE = "com.google.android.projection.gearhead";

    @Test
    public void packageCommandsTargetOnlyAndroidAutoForPrimaryUser() {
        assertEquals("/system/bin/pm enable --user 0 " + PACKAGE, GuardCommands.enablePackage());
        assertEquals("/system/bin/pm disable-user --user 0 " + PACKAGE, GuardCommands.disablePackage());
        assertEquals("/system/bin/am force-stop " + PACKAGE, GuardCommands.forceStop());
    }
}
