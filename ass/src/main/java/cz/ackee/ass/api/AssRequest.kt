package cz.ackee.ass.api

import com.google.gson.annotations.SerializedName

/**
 * Information that is sent for every request, mostly those that can be collected without user's
 * help. User-defined parameters are stored in [customData].
 */
internal data class AssRequest(
    @SerializedName("deviceModel")
    val deviceModel: String,
    @SerializedName("appVersion")
    val appVersion: String,
    @SerializedName("deviceMake")
    val deviceMake: String,
    @SerializedName("appName")
    val appName: String,
    @SerializedName("osVersion")
    val osVersion: String,
    @SerializedName("platform")
    val platform: String,
    @SerializedName("buildNumber")
    val buildNumber: Int,
    @SerializedName("bundleId")
    val bundleId: String,
    @SerializedName("note")
    val note: String? = null,
    @SerializedName("customData")
    val customData: Map<String, Any>? = null
)
