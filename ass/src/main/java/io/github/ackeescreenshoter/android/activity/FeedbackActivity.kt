package io.github.ackeescreenshoter.android.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.github.dhaval2404.imagepicker.ImagePicker
import io.github.ackeescreenshoter.android.Ass
import io.github.ackeescreenshoter.android.FeedbackData
import io.github.ackeescreenshoter.android.R
import io.github.ackeescreenshoter.android.api.AssRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.File

/**
 * Main activity of the library.
 * User can click on screenshot image to edit it.
 */
internal class FeedbackActivity : AppCompatActivity() {

    companion object {

        const val ARG_APP_NAME = "app_name"
        const val ARG_FEEDBACK_DATA = "feedback_data"
        const val RC_SCREENSHOT_EDIT = 1
        const val RC_UPLOAD_FROM_GALLERY = 2
    }

    /**
     * Data received from calling activity - screenshot and user-defined custom parameters
     */
    private val appName by lazy {
        val provided = intent.getStringExtra(ARG_APP_NAME)
        if (provided.isNullOrEmpty()) {
            applicationInfo.labelRes.let { resId ->
                if (resId == 0) applicationInfo.nonLocalizedLabel.toString() else getString(resId)
            }
        } else provided
    }
    private val feedbackData get() = intent.getParcelableExtra<FeedbackData>(ARG_FEEDBACK_DATA)!!
    private var call: Call<Unit>? = null
    private lateinit var sendItem: MenuItem
    private lateinit var request: AssRequest

    private lateinit var toolbar: Toolbar
    private lateinit var listParameters: RecyclerView
    private lateinit var editTextFeedback: EditText
    private lateinit var imgScreenshot: ImageView
    private lateinit var layoutScreenshot: View
    private lateinit var btnUploadFromGallery: Button

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ass_feedback_layout)

        toolbar = findViewById(R.id.ass_toolbar)
        listParameters = findViewById(R.id.ass_list_parameters)
        editTextFeedback = findViewById(R.id.ass_edit_text_feedback)
        imgScreenshot = findViewById(R.id.ass_img_screenshot)
        layoutScreenshot = findViewById(R.id.ass_layout_screenshot)
        btnUploadFromGallery = findViewById(R.id.ass_btn_upload_from_gallery)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val packageInfo = packageManager.getPackageInfo(packageName, 0)

        request = AssRequest(
            deviceModel = Build.MODEL,
            appVersion = packageInfo.versionName,
            deviceMake = Build.MANUFACTURER,
            appName = appName,
            osVersion = "${Build.VERSION.RELEASE} (api ${Build.VERSION.SDK_INT})",
            platform = "android",
            buildNumber = @Suppress("DEPRECATION") packageInfo.versionCode,
            bundleId = packageName,
            customData = feedbackData.customData
        )

        loadScreenshot()

        listParameters.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ParameterAdapter(listOf(
                getString(R.string.ass_app_name) to request.appName,
                getString(R.string.ass_app_version) to "${request.appVersion} (${request.buildNumber})",
                getString(R.string.ass_package_name) to request.bundleId,
                getString(R.string.ass_device_model) to "${request.deviceMake} ${request.deviceModel}",
                getString(R.string.ass_os_version) to "${request.platform} ${request.osVersion}"
            ) + feedbackData.customData.toList()).apply {
                notifyDataSetChanged()
            }

            addItemDecoration(object : RecyclerView.ItemDecoration() {
                val space = resources.getDimension(R.dimen.parameter_item_spacing).toInt()

                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    super.getItemOffsets(outRect, view, parent, state)
                    if (parent.getChildAdapterPosition(view) != 0) {
                        outRect.top = space
                    }
                }
            })
        }

        editTextFeedback.setOnTouchListener { v, event ->
            if (v.hasFocus()) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_SCROLL) {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }

        layoutScreenshot.setOnClickListener {
            startActivityForResult(Intent(this, EditActivity::class.java).apply {
                putExtra(EditActivity.SCREENSHOT_BITMAP_URI, feedbackData.screenshotUri)
            }, RC_SCREENSHOT_EDIT)
        }

        btnUploadFromGallery.setOnClickListener {
            openGalleryPicker()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        call?.let {
            if (!it.isCanceled) {
                it.cancel()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_SCREENSHOT_EDIT -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.getParcelableExtra<Uri>(EditActivity.SCREENSHOT_BITMAP_URI)?.let { uri ->
                        intent.putExtra(ARG_FEEDBACK_DATA, feedbackData.copy(screenshotUri = uri))
                        loadScreenshot()
                    }
                }
            }
            RC_UPLOAD_FROM_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        intent.putExtra(ARG_FEEDBACK_DATA, feedbackData.copy(screenshotUri = uri))
                        loadScreenshot()
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ass_send_feedback, menu)
        sendItem = menu.findItem(R.id.ass_menu_send_feedback)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item == sendItem) {
            send()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadScreenshot() {
        imgScreenshot.load(feedbackData.screenshotUri) {
            memoryCachePolicy(CachePolicy.DISABLED)
        }
    }

    private fun openGalleryPicker() {
        ImagePicker.with(this)
            .compress(1024)
            .maxResultSize(1080, 1080)
            .galleryOnly()
            .start(RC_UPLOAD_FROM_GALLERY)
    }

    /**
     * Sets user-provided description to the [request] and sends it together with the screenshot
     * to the server.
     */
    private fun send() {
        request = request.copy(note = editTextFeedback.text?.toString().let { text ->
            if (text.isNullOrEmpty()) {
                null
            } else {
                text
            }
        })

        val screenshotUri = feedbackData.screenshotUri
        val file = if (screenshotUri.scheme == "file") {
            // If this URI is from file (from gallery), create file as is
            File(screenshotUri.path!!)
        } else {
            // Otherwise it's a content URI and we need to append the cache path
            File(cacheDir, screenshotUri.path!!)
        }

        val multipartScreenshot = MultipartBody.Part.createFormData(
            "screenshot",
            "screenshot.jpg",
            file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        )

        val multipartJson = Ass.moshi.adapter(AssRequest::class.java)
            .toJson(request)
            .toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        sendItem.actionView = ProgressBar(this)
        editTextFeedback.isEnabled = false
        layoutScreenshot.isClickable = false

        call = Ass.apiDescription.uploadFeedback(multipartScreenshot, multipartJson).apply {
            enqueue(object : Callback<Unit> {
                private fun fail() {
                    Toast.makeText(this@FeedbackActivity, getString(R.string.ass_report_not_sent), Toast.LENGTH_LONG).show()
                    sendItem.actionView = null
                    editTextFeedback.isEnabled = true
                    layoutScreenshot.isClickable = true
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    t.printStackTrace()
                    fail()
                }

                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@FeedbackActivity, getString(R.string.ass_report_sent), Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        HttpException(response).printStackTrace()
                        fail()
                    }
                }
            })
        }
    }
}
