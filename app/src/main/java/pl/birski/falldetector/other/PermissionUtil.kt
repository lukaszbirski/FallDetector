package pl.birski.falldetector.other

import android.Manifest
import android.content.Context
import pub.devrel.easypermissions.EasyPermissions

object PermissionUtil {

    fun hasMessagesPermission(context: Context) = EasyPermissions.hasPermissions(
        context,
        Manifest.permission.SEND_SMS
    )

    fun returnPermissionsArray() = arrayOf(
        Manifest.permission.SEND_SMS
    )
}
