package com.rokoblak.blescan.devices

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.rokoblak.blescan.exceptions.PermissionNotGrantedException
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PermissionsChecker @Inject constructor(@ApplicationContext private val context: Context) {

    private fun checkPermission(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

   fun checkMissingPermissions(): PermissionNotGrantedException? {
       return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
           if (!checkPermission(Manifest.permission.BLUETOOTH_SCAN)) {
               PermissionNotGrantedException(Manifest.permission.BLUETOOTH_SCAN)
           } else {
               null
           }
       } else {
           if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
               PermissionNotGrantedException(Manifest.permission.ACCESS_FINE_LOCATION)
           } else {
               null
           }
       }
   }
}
