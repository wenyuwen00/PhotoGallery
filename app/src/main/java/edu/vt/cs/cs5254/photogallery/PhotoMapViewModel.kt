package edu.vt.cs.cs5254.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.vt.cs.cs5254.photogallery.api.FlickrFetchr
import edu.vt.cs.cs5254.photogallery.api.GalleryItem

class PhotoMapViewModel: ViewModel() {
    val galleryItemsLiveData = FlickrFetchr.galleryItemsLiveData

    var geoGalleryItemMapLiveData: LiveData<Map<String, GalleryItem>> =
        Transformations.switchMap(galleryItemsLiveData) { items ->
            val geoGalleryItemMap =
                items.filterNot { it.latitude == "0" && it.longitude == "0" }
                    .associateBy { it.id }
            MutableLiveData<Map<String, GalleryItem>>(geoGalleryItemMap)
        }
    fun reLoadPhotos() {
        FlickrFetchr.fetchPhotos(true)
        geoGalleryItemMapLiveData = Transformations.switchMap(galleryItemsLiveData) { items ->
            val geoGalleryItemMap =
                items.filterNot { it.latitude == "0" && it.longitude == "0" }
                    .associateBy { it.id }
            MutableLiveData<Map<String, GalleryItem>>(geoGalleryItemMap)
        }
    }


}