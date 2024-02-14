package io.github.ackeescreenshoter.android.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import java.io.File
import java.io.FileOutputStream

internal fun View.captureView(window: Window, bitmapCallback: (Bitmap) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Above Android O, use PixelCopy
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val location = IntArray(2)
        getLocationInWindow(location)
        PixelCopy.request(
            window,
            Rect(location[0], location[1], location[0] + width, location[1] + height),
            bitmap,
            {
                if (it == PixelCopy.SUCCESS) {
                    bitmapCallback.invoke(bitmap)
                }
            },
            Handler(Looper.getMainLooper())
        )
    } else {
        bitmapCallback.invoke(drawToBitmap())
    }
}

/**
 * Stores the bitmap into a cache directory defined by [ScreenshotFileProvider]. The image
 * stored in the cache is overwritten each time this method is called.
 * Allowed directories are defined in /res/xml/file_provider_paths.xml and stores the image under
 * [name] name or `/screenshots/image.jpg` if name is not specified.
 */
internal fun Context.storeBitmapToCache(bitmap: Bitmap, name: String? = null): Uri {
    val imagePath = File(cacheDir, "screenshots").apply { mkdirs() }
    val newFile = File(imagePath, name ?: "image.jpg")

    FileOutputStream(newFile).let {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        it.close()
    }

    return FileProvider.getUriForFile(this, "$packageName.io.github.ackeescreenshoter.android.screenshots", newFile)
}
