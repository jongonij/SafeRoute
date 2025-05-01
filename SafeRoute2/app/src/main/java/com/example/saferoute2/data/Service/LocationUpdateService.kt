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
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class LocationUpdateService : Service() {

    private lateinit var controladorLocalizacion: ControladorLocalizacion
    private val TAG = "LocationService"

    override fun onCreate() {
        super.onCreate()
        // Asegurar que Firebase esté inicializado
        FirebaseApp.initializeApp(this)
        Log.d(TAG, "Firebase inicializado en el servicio")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Verificar si el usuario está autenticado
        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario == null) {
            Log.e(TAG, "Usuario no autenticado. Cancelando servicio.")
            stopSelf()
            return START_NOT_STICKY
        }

        // Crear canal de notificación si es necesario (solo en Android 8.0 y superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "LocationUpdateChannel"
            val channel = NotificationChannel(
                channelId,
                "Actualización de ubicación",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

        Log.d(TAG, "Servicio de ubicación iniciado correctamente.")
        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        if (::controladorLocalizacion.isInitialized) {
            controladorLocalizacion.detenerActualizacionUbicacion()
        }
        Log.d(TAG, "Servicio de ubicación detenido.")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
