package com.example.saferoute2.data.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.saferoute2.data.Service.LocationUpdateService
import com.google.firebase.auth.FirebaseAuth



/**
 * BootReceiver es un BroadcastReceiver que se activa cuando el dispositivo se inicia o el usuario
 * desbloquea la pantalla. Su función principal es iniciar el servicio de actualización de ubicación
 * en segundo plano.
 *
 * @constructor Crea una instancia de BootReceiver.
 */
class BootReceiver : BroadcastReceiver() {
    /**
     * Método llamado cuando se recibe un broadcast. Verifica la acción del intent y, si es
     * ACTION_BOOT_COMPLETED o ACTION_USER_PRESENT, inicia el servicio de ubicación.
     *
     * @param context El contexto de la aplicación.
     * @param intent El intent que activó el receptor.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        // Verificar el tipo de acción que recibió
        Log.d("BootReceiver", "Acción recibida: ${intent.action}")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_USER_PRESENT) {
            val serviceIntent = Intent(context, LocationUpdateService::class.java)
            context.startForegroundService(serviceIntent)  // Iniciar el servicio de ubicación
            Log.d("BootBroadcastReceiver", "Servicio de ubicación iniciado.")
        }
    }
}
