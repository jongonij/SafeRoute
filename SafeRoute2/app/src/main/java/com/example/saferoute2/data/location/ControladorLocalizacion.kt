package com.example.saferoute2.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.example.saferoute2.data.model.Ubicacion
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ControladorLocalizacion(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest = LocationRequest.create().apply {
        interval = 10000 // cada 10s
        fastestInterval = 5000 // mínimo cada 5s
        priority = Priority.PRIORITY_HIGH_ACCURACY
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                actualizarUbicacionEnFirebase(location)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun iniciarActualizacionUbicacion() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    fun detenerActualizacionUbicacion() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun actualizarUbicacionEnFirebase(location: Location) {
        val sharedPrefs = context.getSharedPreferences("SafeRoutePrefs", Context.MODE_PRIVATE)
        val uid = sharedPrefs.getString("user_uid", null) ?: return
        val ref = FirebaseDatabase.getInstance().getReference("usuarios").child(uid).child("ubicacionActual")

        val ubicacion = Ubicacion(
            latitud = location.latitude,
            longitud = location.longitude,
            timestamp = System.currentTimeMillis()
        )

        ref.setValue(ubicacion)
            .addOnSuccessListener {
                Log.d("GPS", "Ubicación actualizada correctamente")
            }
            .addOnFailureListener {
                Log.e("GPS", "Error al actualizar ubicación: ${it.message}")
            }
    }
}
