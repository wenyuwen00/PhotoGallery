package edu.vt.cs.cs5254.photogallery

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import edu.vt.cs.cs5254.photogallery.api.FlickrFetchr

class PhotoGalleryViewModel: ViewModel() {
    val galleryItemsLiveData = FlickrFetchr.galleryItemsLiveData

    fun loadPhotos() {
        FlickrFetchr.fetchPhotos(false)
    }
    fun reLoadPhotos() {
        FlickrFetchr.fetchPhotos(true)
    }


    fun storeThumbnail(id: String, drawable: Drawable){
        FlickrFetchr.storeThumbnail(id, drawable)
    }
}