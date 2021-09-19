package edu.vt.cs.cs5254.photogallery.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object FlickrFetchr {

    const val TAG = "FlickrFetchr"
    private val flickrApi: FlickrApi
    val galleryItemsLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    fun fetchPhotos(force: Boolean){


        if(galleryItemsLiveData.value != null && !force) return
        Log.d(TAG, "NETWORK REQUEST for gallery items")
        val flickrRequest: Call<FlickrResponse> = flickrApi.fetchPhotos()
        flickrRequest.enqueue(object : Callback<FlickrResponse> {
            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }

            override fun onResponse(
                call: Call<FlickrResponse>,
                response: Response<FlickrResponse>
            ) {
                Log.d(TAG, "Response received")
                val flickrResponse: FlickrResponse? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems
                    ?: mutableListOf()
                galleryItems = galleryItems.filterNot {
                    it.url.isBlank()
                }
                galleryItemsLiveData.value = galleryItems
            }
        })
    }

    fun storeThumbnail(id: String, drawable: Drawable){
        galleryItemsLiveData.value?.find { it.id == id }?.drawable = drawable
    }

    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        Log.d(TAG, "NETWORK REQUEST for photo $url")
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        Log.i(TAG, "Decoded bitmap=$bitmap from Response=$response")
        return bitmap
    }
}