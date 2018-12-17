package cz.ackee.ass

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.squareup.moshi.Moshi
import com.squareup.seismic.ShakeDetector
import cz.ackee.ass.activity.EditActivity
import cz.ackee.ass.activity.FeedbackActivity
import cz.ackee.ass.api.ApiDescription
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Public API of the library.
 *
 * Library needs to be [initialize]d first with url of server and authToken used in 'Authorization'
 * header.
 *
 * Afterwards, [FeedbackActivity] is started whenever the device is shaken
 * with parameters and screenshot taken from currently resumed activity.
 *
 * Custom parameters can be added to [globalParameters] and [localParameters] and will be uploaded
 * together with other predefined device-specific information.
 */
@Suppress("unused")
object Ass {

    internal lateinit var moshi: Moshi
    internal lateinit var apiDescription: ApiDescription

    /**
     * Detector for each created activity. When the activity is destroyed, reference to it is
     * removed and detector destroyed. At most single detector should be active at a time.
     *
     * Implementation note: another option is to have just a single detector but then we would
     * have to manage by ourselves which activity is currently resumed in order to provide the
     * correct screenshot.
     */
    private val detectors: MutableMap<Activity, ShakeDetector> = mutableMapOf()

    /**
     * Map of parameters scoped to an [Activity]. Leaks should not occur because we are removing
     * references to activities when they are destroyed.
     *
     * Internally store values of [Any] type but externally allow to add only types defined in
     * [AssParameter] - [String], [Int], [Long], [Float], [Double], [Boolean].
     */
    private val localParameters: MutableMap<Activity, Map<String, Any>> = mutableMapOf()

    /**
     * Internally store values of [Any] type but externally allow to add only types defined in
     * [AssParameter] - [String], [Int], [Long], [Float], [Double], [Boolean].
     */
    private var globalParameters: Map<String, Any> = mutableMapOf()

    /**
     * Set of activities that should not react to shake gesture.
     */
    private val disabledActivities: MutableSet<Class<out Activity>> = mutableSetOf(
        FeedbackActivity::class.java, EditActivity::class.java
    )

    /**
     * Sensitivity of shake gesture.
     */
    private var shakeSensitivity: Sensitivity = Sensitivity.Medium

    /**
     * Levels of sensitivity.
     *
     * Note: Sensitivity levels smaller than 9.807 will result into permanently activated
     * acceleration sensor even if the device is not moving.
     */
    enum class Sensitivity(internal val sensitivity: Int) {
        Light(ShakeDetector.SENSITIVITY_LIGHT),
        Medium(ShakeDetector.SENSITIVITY_MEDIUM),
        Hard(ShakeDetector.SENSITIVITY_HARD)
    }

    /**
     * Replaces all global parameters with [parameters].
     */
    fun setGlobalParameters(vararg parameters: AssParameter) {
        globalParameters = parameters.toMap()
    }

    /**
     * Adds [parameters] to global parameters.
     */
    fun addGlobalParameters(vararg parameters: AssParameter) {
        globalParameters += parameters.toMap()
    }

    /**
     * Removes all global parameters whose id is defined in [parameters].
     */
    fun removeGlobalParameters(vararg parameters: String) {
        globalParameters -= parameters
    }

    /**
     * Returns a copy of all global parameters.
     */
    fun getGlobalParameters(): Map<String, Any> {
        return globalParameters.toMap()
    }

    /**
     * Replaces all local parameters of an [activity] with [parameters].
     */
    fun setLocalParameters(activity: Activity, vararg parameters: AssParameter) {
        localParameters[activity] = parameters.toMap()
    }

    /**
     * Adds [parameters] to local parameters of an [activity].
     */
    fun addLocalParameters(activity: Activity, vararg parameters: AssParameter) {
        localParameters[activity] = localParameters[activity]!! + parameters.toMap()
    }

    /**
     * Removes all local parameters of an [activity] whose id is defined in [parameters].
     */
    fun removeLocalParameters(activity: Activity, parameters: Set<String>) {
        localParameters[activity] = localParameters[activity]!! - parameters
    }

    /**
     * Returns a copy of local parameters of an [activity].
     */
    fun getLocalParameters(activity: Activity): Map<String, Any> {
        return localParameters[activity]?.toMap() ?: emptyMap()
    }

    /**
     * Add activities that should not react to shake gesture.
     */
    fun addDisabledActivities(vararg classes: Class<out Activity>) {
        disabledActivities += classes
    }

    /**
     * Make disabled activities react to shake gestures again.
     *
     * Note: If an activity is already created, it needs to be recreated in order to properly start
     * reacting again.
     */
    fun removeDisabledActivities(vararg classes: Class<out Activity>) {
        disabledActivities -= classes
    }

    /**
     * Sets sensitivity of shake gesture.
     *
     * Note: If an activity is already created, it needs to be recreated in order to properly start
     * reacting again.
     */
    fun setShakeSensitivity(sensitivity: Sensitivity) {
        shakeSensitivity = sensitivity
    }

    /**
     * Initialize the library with [url] of the server and [authToken] required by the server.
     */
    fun initialize(app: Application, url: String, authToken: String, enableLogging: Boolean = false) {
        moshi = Moshi.Builder().build()
        apiDescription = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(OkHttpClient.Builder().apply {
                addNetworkInterceptor { chain ->
                    chain.proceed(chain.request()
                        .newBuilder()
                        .header("X-Version", BuildConfig.VERSION_CODE.toString())
                        .header("Authorization", "Bearer $authToken")
                        .build())
                }
                if (enableLogging) {
                    addNetworkInterceptor(HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                }
            }.build())
            .build()
            .create(ApiDescription::class.java)

        val manager = app.getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager

        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {

            private fun Activity.isDisabled() = disabledActivities.contains(this::class.java)

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity.isDisabled()) return

                localParameters[activity] = emptyMap()
                lateinit var shakeDetector: ShakeDetector
                shakeDetector = ShakeDetector {
                    // If we don't stop here, it is possible for shake detector to generate
                    // additional events before Activity.onPause is called.
                    shakeDetector.stop()
                    open(activity, getLocalParameters(activity))
                }.apply {
                    setSensitivity(shakeSensitivity.sensitivity)
                }
                detectors[activity] = shakeDetector
            }

            override fun onActivityStarted(activity: Activity) {
                // not used
            }

            override fun onActivityResumed(activity: Activity) {
                if (activity.isDisabled()) return

                detectors[activity]?.start(manager)
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
                // not used
            }

            override fun onActivityPaused(activity: Activity) {
                detectors[activity]?.stop()
            }

            override fun onActivityStopped(activity: Activity) {
                // not used
            }

            override fun onActivityDestroyed(activity: Activity) {
                detectors.remove(activity)
                localParameters.remove(activity)
            }
        })
    }

    /**
     * Starts [FeedbackActivity] without need for shaking the device.
     */
    fun open(activity: Activity, vararg parameters: AssParameter) {
        if (!this::apiDescription.isInitialized) {
            throw Exception("Library not initialized. Did you forget to call Ass.initialize()?")
        }
        open(activity, parameters.toMap())
    }

    /**
     * Converts `vararg parameters: AssParameter` to type-unsafe map
     */
    private fun Array<out AssParameter>.toMap(): Map<String, Any> {
        return this.map { it.key to it.value }.toMap()
    }

    /**
     * Creates screenshot of [activity], stores it to cache and send it to [FeedbackActivity]
     * together with global and local parameters.
     */
    private fun open(activity: Activity, parameters: Map<String, Any>) {
        val screenshot = activity.window.decorView.createBitmap()
        val screenshotUri = activity.storeBitmapToCache(screenshot)
        activity.startActivity(Intent(activity, FeedbackActivity::class.java).apply {
            putExtra(FeedbackActivity.ARG_FEEDBACK_DATA, FeedbackData(
                screenshotUri,
                HashMap(parameters + globalParameters)
            ))
        })
    }
}

/**
 * Users can send custom data as a part of each [Request][cz.ackee.ass.api.AssRequest] but it must
 * be one of allowed types. This way we can add data of allowed types in a type-safe manner.
 */
sealed class AssParameter(val key: kotlin.String) {
    abstract val value: Any

    class String(key: kotlin.String, override val value: kotlin.String): AssParameter(key)
    class Int(key: kotlin.String, override val value: kotlin.Int): AssParameter(key)
    class Long(key: kotlin.String, override val value: kotlin.Long): AssParameter(key)
    class Float(key: kotlin.String, override val value: kotlin.Float): AssParameter(key)
    class Double(key: kotlin.String, override val value: kotlin.Double): AssParameter(key)
    class Boolean(key: kotlin.String, override val value: kotlin.Boolean): AssParameter(key)
}

infix fun String.withValue(parameter: String) = AssParameter.String(this, parameter)
infix fun String.withValue(parameter: Int) = AssParameter.Int(this, parameter)
infix fun String.withValue(parameter: Long) = AssParameter.Long(this, parameter)
infix fun String.withValue(parameter: Float) = AssParameter.Float(this, parameter)
infix fun String.withValue(parameter: Double) = AssParameter.Double(this, parameter)
infix fun String.withValue(parameter: Boolean) = AssParameter.Boolean(this, parameter)
