package com.qubitons.attendancetracker.ui.home

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.qubitons.attendancetracker.GeoLocationManager
import com.qubitons.attendancetracker.R
import java.util.logging.Logger

class LocationForegroundService : Service() {
    val LOG = Logger.getLogger(LocationForegroundService::class.java.name)

    private val CHANNEL_ID = "LocationForegroundService"
    private val NOTIFICATION_ID = 123

    private lateinit var locationManager: GeoLocationManager
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {

                // Handle the received location updates here
                LOG.info("longitude: ${location.longitude} latitude: ${location.latitude}")
            }

        }
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = GeoLocationManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        locationManager.startLocationTracking(locationCallback)
        // No need to start the service immediately upon fragment load
        // The service will be started when the "Check In" button is clicked
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.stopLocationTracking(locationCallback)
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Tracking")
            .setContentText("Tracking your location...")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

