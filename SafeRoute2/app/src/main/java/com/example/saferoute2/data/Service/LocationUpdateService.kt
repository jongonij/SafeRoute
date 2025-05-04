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
import com.google.firebase.database.FirebaseDatabase
/**
 * Servicio que se ejecuta en segundo plano para compartir la ubicación del usuario autenticado
 * a través de Firebase Realtime Database. Funciona como un servicio en primer plano en Android 8.0+.
 *
 * Este servicio utiliza la clase [ControladorLocalizacion] para iniciar y detener las actualizaciones
 * de ubicación.
 */
class LocationUpdateService : Service() {

    private lateinit var controladorLocalizacion: ControladorLocalizacion
    private val TAG = "LocationService"
    /**
     * Método llamado al crear el servicio. Inicializa Firebase y habilita la persistencia local.
     */
    override fun onCreate() {
        super.onCreate()
        // Asegurar que Firebase esté inicializado
        FirebaseApp.initializeApp(this)
        Log.d(TAG, "Firebase inicializado en el servicio")
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

    }
    /**
     * Método llamado cuando se inicia el servicio. Comprueba si el usuario está autenticado.
     * Si es así, se inicia el servicio en primer plano (foreground service) y comienzan las
     * actualizaciones de ubicación.
     *
     * @param intent El intent que inicia el servicio.
     * @param flags Información adicional sobre cómo se inicia el servicio.
     * @param startId Identificador único del inicio del servicio.
     * @return Constante que indica cómo se debe comportar el sistema si el servicio es eliminado.
     */
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

    /**
     * Método llamado cuando el servicio se destruye. Detiene las actualizaciones de ubicación
     * si el controlador fue inicializado.
     */
    override fun onDestroy() {
        super.onDestroy()
        if (::controladorLocalizacion.isInitialized) {
            controladorLocalizacion.detenerActualizacionUbicacion()
        }
        Log.d(TAG, "Servicio de ubicación detenido.")
    }
    /**
     * Método obligatorio para servicios enlazados. En este caso, el servicio no es enlazado,
     * por lo tanto retorna null. Aun así, se debe implementar para cumplir con la interfaz de servicio.
     *
     * @param intent El intent que se usó para enlazar el servicio.
     * @return Siempre null, ya que el servicio no permite enlace.
     */
    override fun onBind(intent: Intent?): IBinder? = null
}
