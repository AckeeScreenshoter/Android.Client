package cz.ackee.ass.api

/**
 * Information that is sent for every request, mostly those that can be collected without user's
 * help. User-defined parameters are stored in [customData].
 */
internal data class AssRequest(
    val deviceModel: String,
    val appVersion: String,
    val deviceMake: String,
    val appName: String,
    val osVersion: String,
    val platform: String,
    val buildNumber: Int,
    val bundleId: String,
    val note: String? = null,
    val customData: Map<String, Any>? = null
)
