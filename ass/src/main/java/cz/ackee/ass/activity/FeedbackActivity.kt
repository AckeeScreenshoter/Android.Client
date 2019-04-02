package cz.ackee.ass.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.squareup.picasso.Picasso
import cz.ackee.ass.Ass
import cz.ackee.ass.FeedbackData
import cz.ackee.ass.R
import cz.ackee.ass.api.AssRequest
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
        const val ARG_FEEDBACK_DATA = "feedback_data"
        const val RC_SCREENSHOT_EDIT = 1
    }

    /**
     * Data received from calling activity - screenshot and user-defined custom parameters
     */
    private val feedbackData by lazy { intent.getParcelableExtra<FeedbackData>(ARG_FEEDBACK_DATA) }
    private var call: Call<Unit>? = null
    private lateinit var sendItem: MenuItem
    private lateinit var request: AssRequest

    private lateinit var toolbar: Toolbar
    private lateinit var listParameters: RecyclerView
    private lateinit var editTextFeedback: EditText
    private lateinit var imgScreenshot: ImageView
    private lateinit var layoutScreenshot: View

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ass_feedback_layout)

        toolbar = findViewById(R.id.ass_toolbar)
        listParameters = findViewById(R.id.ass_list_parameters)
        editTextFeedback = findViewById(R.id.ass_edit_text_feedback)
        imgScreenshot = findViewById(R.id.ass_img_screenshot)
        layoutScreenshot = findViewById(R.id.ass_layout_screenshot)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val packageInfo = packageManager.getPackageInfo(packageName, 0)

        request = AssRequest(
            deviceModel = Build.MODEL,
            appVersion = packageInfo.versionName,
            deviceMake = Build.MANUFACTURER,
            appName = applicationInfo.labelRes.let { resId ->
                if (resId == 0) {
                    applicationInfo.nonLocalizedLabel.toString()
                } else {
                    getString(resId)
                }
            },
            osVersion = Build.VERSION.SDK_INT.toString(),
            platform = "android",
            buildNumber = @Suppress("DEPRECATION") packageInfo.versionCode,
            bundleId = packageName,
            customData = feedbackData.customData
        )

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

        Picasso.with(this).invalidate(feedbackData.screenshotUri)
        Picasso.with(this).load(feedbackData.screenshotUri).into(imgScreenshot)

        layoutScreenshot.setOnClickListener {
            startActivityForResult(Intent(this, EditActivity::class.java).apply {
                putExtra(EditActivity.SCREENSHOT_BITMAP_URI, feedbackData.screenshotUri)
            }, RC_SCREENSHOT_EDIT)
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
        if (requestCode == RC_SCREENSHOT_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                data?.getParcelableExtra<Uri>(EditActivity.SCREENSHOT_BITMAP_URI)?.let {
                    Picasso.with(this).invalidate(it)
                    Picasso.with(this).load(it).into(imgScreenshot)
                }
            }
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item == sendItem) {
            send()
            return true
        }
        return super.onOptionsItemSelected(item)
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
        val file = File(cacheDir, screenshotUri.path)

        val multipartScreenshot = MultipartBody.Part.createFormData(
            "screenshot",
            "screenshot.jpg",
            RequestBody.create(MediaType.parse("image/jpeg"), file)
        )
        val multipartJson = RequestBody.create(
            MediaType.parse("application/json"),
            Ass.gson.toJson(request)
        )

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
