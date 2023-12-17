package com.bb.link.util


import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtil {
    private val TAG = "PermissionUtil"
    val PERMISSIONS_REQUEST_CODE = 123

    fun requestP2PPermissions(activity: Activity):Boolean {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        /**
         * P2p在定位权限上 需要使用精确的定位
         */
        return if (!arePermissionsGranted(activity,permissions)) {
            Log.d(TAG,"没权限？")
            ActivityCompat.requestPermissions(activity, permissions, PERMISSIONS_REQUEST_CODE)
            false
        } else {
            // 权限已经被授予，执行你的操作
            Log.d(TAG,"赋予了权限了")
            true
        }
    }

    private fun arePermissionsGranted(activity: Activity,permissions: Array<String>): Boolean {
        for (permission in permissions) {
            Log.d("LZP","permission:${permission}")
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d("LZP","no:${permission}")
                return false
            }
        }
        return true
    }

    fun areAllPermissionsGranted(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            Log.d(TAG,"${result}")
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}