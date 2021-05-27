package io.github.ackeescreenshoter.ass

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import io.github.ackeescreenshoter.ass.activity.FeedbackActivity

/**
 * Data that is passed from calling activity to [FeedbackActivity].
 */
internal data class FeedbackData(
    val screenshotUri: Uri,
    val customData: HashMap<String, Any>
) : Parcelable {

    @Suppress("UNCHECKED_CAST")
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Uri::class.java.classLoader)!!,
        parcel.readSerializable() as HashMap<String, Any>
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(screenshotUri, flags)
        parcel.writeSerializable(customData)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FeedbackData> {
        override fun createFromParcel(parcel: Parcel): FeedbackData {
            return FeedbackData(parcel)
        }

        override fun newArray(size: Int): Array<FeedbackData?> {
            return arrayOfNulls(size)
        }
    }
}
