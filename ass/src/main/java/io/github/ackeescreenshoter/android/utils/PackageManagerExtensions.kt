package io.github.ackeescreenshoter.android.utils

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int): PackageInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
    }
}
