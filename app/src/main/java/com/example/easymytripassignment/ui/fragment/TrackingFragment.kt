package com.example.easymytripassignment.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.easymytripassignment.R
import com.example.easymytripassignment.db.Run
import com.example.easymytripassignment.other.Constants.ACTION_PAUSE_SERVICE
import com.example.easymytripassignment.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.easymytripassignment.other.Constants.ACTION_STOP_SERVICE
import com.example.easymytripassignment.other.Constants.MAP_ZOOM
import com.example.easymytripassignment.other.Constants.POLYLINE_COLOR
import com.example.easymytripassignment.other.Constants.POLYLINE_WIDTH
import com.example.easymytripassignment.other.TrackingUtility
import com.example.easymytripassignment.services.Polyline
import com.example.easymytripassignment.services.TrackingService
import com.example.easymytripassignment.ui.viewModel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private val viewModel : MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints  = mutableListOf<Polyline>()

    private var map : GoogleMap?= null
    private var curTimeMillis =0L
    private var weight = 80f
    private var menu : Menu? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        btnToggleRun.setOnClickListener {
          toggleRun()
        }

        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunSaveToDb()
        }

        mapView.getMapAsync{
            map = it
            addAllPolylines()
        }
        subscribeToObservers()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }



    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
curTimeMillis = it
            val formattedTime = TrackingUtility.getFormattedStopwatchTime(curTimeMillis, true)
            tvTimer.text = formattedTime
        })


    }

    private fun toggleRun(){
        if (isTracking){
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }
        else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu = menu

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (curTimeMillis>0L){
            this.menu?.getItem(0)?.isVisible = true
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking -> {
                showCancelDialog()
            }
        }


        return super.onOptionsItemSelected(item)

    }


    private fun showCancelDialog(){
        val dialog = MaterialAlertDialogBuilder(requireContext(),R.style.AlertDialogTheme)
            .setTitle("Cancel the run?")
            .setMessage("Are you sure to cancel the run and delete all its data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("yes"){_,_->
                stopRun()
            }
            .setNegativeButton("no"){dialogInterface,_->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    private fun stopRun(){
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment2)
    }



    private fun updateTracking(isTracking : Boolean){
        this.isTracking = isTracking
        if(!isTracking){

            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        }
        else{
            btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser(){
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty())  {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last()
                        , MAP_ZOOM
                )
            )
        }
    }

    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints){
            for (pos in polyline){
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )

    }

    private fun endRunSaveToDb(){
        map?.snapshot {
            bmp ->
            var distanceInMeters =0
            for (polyline in pathPoints){
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed = round((distanceInMeters/1000f)/(curTimeMillis/1000f/60/60)*10 )/10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned =((distanceInMeters/1000f)*weight).toInt()
            val run = Run(bmp,dateTimeStamp,avgSpeed,distanceInMeters,curTimeMillis,caloriesBurned)
            viewModel.insertRun(run)

            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),"Saved successfuly",Snackbar.LENGTH_LONG
            ).show()
            stopRun()

        }
    }






    private fun addAllPolylines(){
        for (polyline in pathPoints){
            val polyLineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polyLineOptions)

        }


    }


    private fun addLatestPolyline(){
        if (pathPoints.isNotEmpty() && pathPoints.last().size>1){
val preLastLatLong = pathPoints.last()[pathPoints.last().size-2]
            val lastLatLong = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLong)
                .add(lastLatLong)
            map?.addPolyline(polylineOptions)

        }
    }

    private fun sendCommandToService(action : String) = Intent(requireContext(),TrackingService::class.java).also {
        it.action = action
        requireContext().startService(it)
    }


    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }


    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }









}