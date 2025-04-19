package com.example.saferoute2.data.Service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.saferoute2.data.location.ControladorLocalizacion

class LocationUpdateService : Service() {

    private lateinit var controladorLocalizacion: ControladorLocalizacion

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "LocationUpdateChannel"
            val channel = NotificationChannel(
                channelId,
                "Actualización de ubicación",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            val notification: Notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("SafeRoute")
                .setContentText("Compartiendo tu ubicación en segundo plano.")
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()


            startForeground(1, notification)
        }

        // Iniciar actualizaciones de ubicación
        controladorLocalizacion = ControladorLocalizacion(applicationContext)
        controladorLocalizacion.iniciarActualizacionUbicacion()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        controladorLocalizacion.detenerActualizacionUbicacion()
    }
}
