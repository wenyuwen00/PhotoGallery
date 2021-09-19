package edu.vt.cs.cs5254.photogallery

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.vt.cs.cs5254.gallery.MapViewFragment
import edu.vt.cs.cs5254.photogallery.api.GalleryItem

private const val TAG = "PhotoMapFragment"

class PhotoMapFragment : MapViewFragment(), GoogleMap.OnMarkerClickListener {
    private lateinit var photoMapViewModel: PhotoMapViewModel
    private lateinit var thumbnailDownloader: ThumbnailDownloader<Marker>

    var geoGalleryItemMap = emptyMap<String, GalleryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoMapViewModel = ViewModelProvider(this).get(PhotoMapViewModel::class.java)

        val responseHandler = Handler(Looper.myLooper()!!)
        thumbnailDownloader =
            ThumbnailDownloader(responseHandler) { maker, bitmap ->
                setMarkerIcon(maker, bitmap)
            }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)
        setHasOptionsMenu(true)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.top_menu, menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_reload -> {
                photoMapViewModel.reLoadPhotos()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewLifecycleOwner.lifecycle.addObserver(
            thumbnailDownloader.viewLifecycleObserver
        )

//        photoMapViewModel.galleryItemsLiveData.observe(
//            viewLifecycleOwner,
//            Observer { galleryItems ->
//                updateUI()
//            })
        return onCreateMapView(
            inflater,
            container,
            savedInstanceState,
            R.layout.fragment_photo_map,
            R.id.map_view
        )
        //       return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onMapViewCreated(
            view, savedInstanceState) { googleMap ->
            googleMap.setOnMarkerClickListener(this@PhotoMapFragment)
            photoMapViewModel.geoGalleryItemMapLiveData.observe(
                viewLifecycleOwner,
                Observer { galleryItemMap ->
                    geoGalleryItemMap = galleryItemMap
                    updateUI()
                })

        }

        photoMapViewModel.geoGalleryItemMapLiveData.observe(
            viewLifecycleOwner,
            Observer { galleryItemMap ->
                geoGalleryItemMap = galleryItemMap
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycle.removeObserver(
            thumbnailDownloader.viewLifecycleObserver
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(
            thumbnailDownloader.fragmentLifecycleObserver
        )
    }


    private fun updateUI() {
        // if the fragment is not currently added to its activity, or
        // if there are not gallery items, do not update the UI
        if (!isAdded || geoGalleryItemMap.isEmpty()) {
            return
        }

        Log.i(TAG, "Gallery has has " + geoGalleryItemMap.size + " items")
        // remove all markers, overlays, etc. from the map
        googleMap.clear()

        val bounds = LatLngBounds.Builder()
        for (item in geoGalleryItemMap.values) {
            // log the information of each gallery item with a valid lat-lng
            Log.i(
                TAG,
                "Item id=${item.id} " + "lat=${item.latitude} long=${item.longitude} " + "title=${item.title}"
            )
            // create a lan-lng point for the item and add it to the lat-lng bounds
            val itemPoint = LatLng(item.latitude.toDouble(), item.longitude.toDouble())
            bounds.include(itemPoint)
            // create a marker for the item and add it to the map
            val itemMarker = MarkerOptions().position(itemPoint).title(item.title)
            val marker = googleMap.addMarker(itemMarker)
            marker.tag = item.id
            thumbnailDownloader.queueThumbnail(marker, item.url)
        }
        Log.i(TAG, "Expecting ${geoGalleryItemMap.size} markers on the map")
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        val galleryItemId = marker?.tag
        Log.i(TAG, "the marker id: $galleryItemId has been clicked on.")
        val item = geoGalleryItemMap[galleryItemId]
        val uri = item?.photoPageUri
        return if(uri == null){
            false
        }else{
            val intent = PhotoPageActivity.newIntent(requireContext(), uri)
            startActivity(intent)

            true
        }

    }

}