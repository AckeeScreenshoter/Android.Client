package io.github.ackeescreenshoter.android.utils

import android.content.Context

@Suppress("DEPRECATION")
internal val Context.appVersionCode: Int
    get() = packageManager.getPackageInfo(packageName, 0).versionCode
