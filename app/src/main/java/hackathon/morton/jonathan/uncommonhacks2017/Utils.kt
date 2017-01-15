package hackathon.morton.jonathan.uncommonhacks2017

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.support.v4.content.ContextCompat


/**
 * Created by Jonathan Morton on 1/14/17.
 */
object Utils {

    /**
     * From: https://github.com/googlesamples/android-RuntimePermissions/blob/master/Application/src/main/java/com/example/android/system/runtimepermissions/PermissionUtil.java

     * @param grantResults
     * *
     * @return
     */
    fun allPermissionsGranted(grantResults: IntArray): Boolean {
        if (grantResults.size < 1) {
            return false
        } else {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
            return true
        }
    }

    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
                context,
                permission) == PackageManager.PERMISSION_GRANTED
    }

    fun canDrawOverlays(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        } else {
            return Settings.canDrawOverlays(context)
        }
    }

    fun canAccessApps(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }
}