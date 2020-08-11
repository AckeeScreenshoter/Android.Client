package cz.ackee.ass

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Creates bitmap from view
 */
internal fun View.createBitmap(): Bitmap {
    return Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888).apply {
        draw(Canvas(this))
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

    return FileProvider.getUriForFile(this, "$packageName.cz.ackee.ass.screenshots", newFile)
}
