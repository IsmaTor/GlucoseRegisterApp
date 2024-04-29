package ismaapp.tortosa.glucoseregister.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    public static boolean checkStoragePermission(Activity activity) {
        // Verificar si se tiene el permiso WRITE_EXTERNAL_STORAGE
        int storagePermission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestStoragePermission(Activity activity) {
        // Solicitar el permiso WRITE_EXTERNAL_STORAGE al usuario
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_EXTERNAL_STORAGE);
    }
}
