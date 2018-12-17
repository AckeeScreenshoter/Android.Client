package cz.ackee.ass.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Description of API.
 */
internal interface ApiDescription {

    @Multipart
    @POST("upload")
    fun uploadFeedback(
        @Part screenshot: MultipartBody.Part,
        @Part("metadata") metadata: RequestBody
    ): Call<Unit>
}
