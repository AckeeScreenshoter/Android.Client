package io.github.ackeescreenshoter.android.utils

import android.content.Context

@Suppress("DEPRECATION")
val Context.appVersionCode: Int
    get() = packageManager.getPackageInfo(packageName, 0).versionCode
