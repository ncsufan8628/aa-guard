package your.pkg.aaguard.ipc;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import your.pkg.aaguard.BuildConfig;
import your.pkg.aaguard.AllowWindow;

public class StateProvider extends ContentProvider {
    public static final String AUTH = BuildConfig.APPLICATION_ID + ".state";
    private static final String PATH_ALLOW = "allow";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Only support content://<AUTH>/allow
        if (uri == null || !PATH_ALLOW.equals(uri.getLastPathSegment())) return null;
        Context ctx = getContext();
        if (ctx == null) return null;
        boolean allowed = AllowWindow.getMasterAllowApp(ctx);
        MatrixCursor c = new MatrixCursor(new String[]{"allowed"});
        c.addRow(new Object[]{ allowed ? 1 : 0 });
        return c;
    }

    @Override public String getType(Uri uri) { return null; }
    @Override public Uri insert(Uri uri, ContentValues values) { return null; }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }
}
