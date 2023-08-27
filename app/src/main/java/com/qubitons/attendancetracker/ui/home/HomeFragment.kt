package com.qubitons.attendancetracker.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.qubitons.attendancetracker.R
import com.qubitons.attendancetracker.databinding.FragmentHomeBinding
import java.util.logging.Logger

class HomeFragment : Fragment() {

    val LOG = Logger.getLogger(HomeFragment::class.java.name)
    private var locationTrackingRequested = false
    private var _binding: FragmentHomeBinding? = null
    private val LOCATION_PERMISSION_CODE = 1000

    private lateinit var latitudeView: TextView
    private lateinit var longitudeView: TextView
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        latitudeView = view.findViewById<TextView>(R.id.latitude_value)
        longitudeView = view.findViewById<TextView>(R.id.longitude_value)
        val startTrackingButton = view.findViewById<Button>(R.id.button_start_tracking)
        val stopTrackingButton = view.findViewById<Button>(R.id.button_stop_tracking)
        ///

        stopTrackingButton.setOnClickListener {
            LOG.info("CHECK OUT button")
            stopLocationService()

            // Show the "Check In" button and hide the "Check Out" button
            startTrackingButton.visibility = View.VISIBLE
            stopTrackingButton.visibility = View.GONE
            locationTrackingRequested = false
        }
        fun startForegroundService() {
            if (requestLocationPermission()) {
                // Start the LocationForegroundService
                val serviceIntent = Intent(requireContext(), LocationForegroundService::class.java)
                ContextCompat.startForegroundService(requireContext(), serviceIntent)
            }

        }
        view.findViewById<Button>(R.id.button_start_tracking).setOnClickListener {
            LOG.info("Button clicked")
            val permissionGranted = requestLocationPermission();
            if (permissionGranted) {
                LOG.info("Tracking started")

                // Hide the "Check In" button and show the "Check Out" button
                startTrackingButton.visibility = View.GONE
                stopTrackingButton.visibility = View.VISIBLE
               locationTrackingRequested = true
                ////////////////////////////////
                startForegroundService()
                ////////////////////////////////
                //locationTrackingRequested = true
                //statusTextView.text = "Started"


            }
        }
    }

    private fun stopLocationService() {
        // Stop the LocationForegroundService
        val serviceIntent = Intent(requireContext(), LocationForegroundService::class.java)
        requireContext().stopService(serviceIntent)
    }

    private fun requestLocationPermission(): Boolean {
        var permissionGranted = false

        // If system os is Marshmallow or Above, we need to request runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val cameraPermissionNotGranted = ContextCompat.checkSelfPermission(
                activity as Context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
            if (cameraPermissionNotGranted){
                val permission = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

                // Display permission dialog
                requestPermissions(permission, LOCATION_PERMISSION_CODE)
            }
            else{
                // Permission already granted
                permissionGranted = true
            }
        }
        else{
            // Android version earlier than M -> no need to request permission
            permissionGranted = true
        }

        return permissionGranted
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}