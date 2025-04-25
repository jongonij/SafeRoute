package com.example.saferoute2.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.saferoute2.R
import com.example.saferoute2.data.Service.LocationUpdateService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configura el fragmento del mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Inicializa FirebaseAuth y FusedLocationProviderClient
        firebaseAuth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


    }

    private fun checkLocationPermission() {
        // Verifica si el permiso ya está concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si no se ha concedido el permiso, lo solicitamos
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Si ya tiene permisos, muestra la ubicación en el mapa
            Toast.makeText(this, "Permisos de ubicación ya concedidos", Toast.LENGTH_SHORT).show()
            getCurrentLocation()
        }
    }

    // Manejo del resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, obtener la ubicación
                getCurrentLocation()
            } else {
                // Si el permiso es denegado, mostramos un mensaje y damos la opción de ir a la configuración
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                openLocationSettings()
            }
        }
    }

    private fun openLocationSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = android.net.Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    private fun getCurrentLocation() {
        // Verifica si el permiso de ubicación fue concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Obtener la última ubicación conocida del usuario
            fusedLocationClient.lastLocation.addOnSuccessListener(this, OnSuccessListener { location ->
                if (location != null) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(MarkerOptions().position(currentLocation).title("Tu Ubicación Actual"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f)) // Ajusta el zoom según sea necesario
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Verifica si se otorgó el permiso antes de acceder a la ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_requests -> {
                val intent = Intent(this, LocationRequestActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()

        // Verifica si el usuario ya está autenticado
        if (firebaseAuth.currentUser != null) {
            // El usuario ya está autenticado, podemos lanzar el servicio
            startLocationUpdateService()
        } else {
            // Si el usuario no está autenticado, redirige al Login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Evita que el usuario regrese a esta actividad
        }
    }

    private fun startLocationUpdateService() {
        val serviceIntent = Intent(this, LocationUpdateService::class.java)
        Log.d("LocationService", "Servicio de ubicación en primer plano iniciado")
        startService(serviceIntent)
    }
}
