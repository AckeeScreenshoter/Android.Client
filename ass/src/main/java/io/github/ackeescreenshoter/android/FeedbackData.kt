package io.github.ackeescreenshoter.android

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat
import io.github.ackeescreenshoter.android.activity.FeedbackActivity

/**
 * Data that is passed from calling activity to [FeedbackActivity].
 */
internal data class FeedbackData(
    val screenshotUri: Uri,
    val customData: HashMap<String, Any>
) : Parcelable {

    @Suppress("UNCHECKED_CAST")
    constructor(parcel: Parcel) : this(
        ParcelCompat.readParcelable(parcel, Uri::class.java.classLoader, Uri::class.java)!!,
        ParcelCompat.readSerializable(parcel, HashMap::class.java.classLoader, HashMap::class.java) as HashMap<String, Any>
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
