package com.ocreboy.rolinitiative.utils

import android.content.Context
import android.content.pm.PackageManager

class AppVersion(private val context: Context) {

    fun getAppVersionName(): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "Version not found"
        } catch (e: PackageManager.NameNotFoundException) {
            "Version not found"
        }
    }
}