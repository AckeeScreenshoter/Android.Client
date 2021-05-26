package io.github.ackeecz.ass.activity

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import io.github.ackeecz.ass.DrawableView
import io.github.ackeecz.ass.R
import io.github.ackeecz.ass.activity.EditActivity.Companion.SCREENSHOT_BITMAP_URI
import io.github.ackeecz.ass.storeBitmapToCache

/**
 * Allows the user to draw over bitmap stored at [Uri] provided by intent's [SCREENSHOT_BITMAP_URI]
 * extra.
 */
internal class EditActivity : AppCompatActivity() {

    companion object {
        const val SCREENSHOT_BITMAP_URI = "screenshot_bitmap_uri"
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screenshot_edit)

        val imgScreenshot = findViewById<DrawableView>(R.id.ass_img_screenshot)
        val btnUndo = findViewById<ImageView>(R.id.undo)
        val btnRedo = findViewById<ImageView>(R.id.redo)
        val btnClose = findViewById<ImageView>(R.id.close)
        val btnDone = findViewById<ImageView>(R.id.done)

        val screenshotUri = intent.getParcelableExtra<Uri>(SCREENSHOT_BITMAP_URI)!!
        val screenshot = contentResolver.openInputStream(screenshotUri).use {
            BitmapFactory.decodeStream(it)!!
        }

        imgScreenshot.setImageBitmap(screenshot)
        imgScreenshot.listener = {
            btnUndo.isEnabled = imgScreenshot.canUndo
            btnRedo.isEnabled = imgScreenshot.canRedo
        }

        btnClose.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        btnDone.setOnClickListener {
            val modifiedScreenshot = imgScreenshot.getFinalBitmap()
            val modifiedScreenshotUri = storeBitmapToCache(modifiedScreenshot)
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(SCREENSHOT_BITMAP_URI, modifiedScreenshotUri)
            })
            finish()
        }

        btnUndo.setOnClickListener {
            imgScreenshot.undo()
        }

        btnRedo.setOnClickListener {
            imgScreenshot.redo()
        }
    }
}
