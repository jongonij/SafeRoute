package com.example.saferoute2.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.toUpperCase
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.saferoute2.R
import com.example.saferoute2.data.Service.LocationUpdateService
import com.example.saferoute2.data.model.Permiso
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var permisosDb: DatabaseReference
    private lateinit var usersDb: DatabaseReference
    private lateinit var miUid: String
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
        permisosDb = FirebaseDatabase.getInstance().getReference("permisos")
        usersDb = FirebaseDatabase.getInstance().getReference("usuarios")
        miUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Cargar las ubicaciones compartidas
        cargarUbicacionesCompartidas()

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
    private fun cargarUbicacionesCompartidas() {
        permisosDb.orderByChild("receptorId").equalTo(miUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (permisoSnapshot in snapshot.children) {
                        val permiso = permisoSnapshot.getValue(Permiso::class.java)
                        if (permiso != null && permiso.estado == "ACEPTADO") {
                            // Obtener la ubicación compartida de este permiso
                            usersDb.child(permiso.solicitanteId).child("ubicacionActual")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(data: DataSnapshot) {
                                        val latitud = data.child("latitud").getValue(Double::class.java)
                                        val longitud = data.child("longitud").getValue(Double::class.java)
                                        if (latitud != null && longitud != null) {
                                            val ubicacion = LatLng(latitud, longitud)
                                            mostrarUbicacionEnMapa(ubicacion, permiso.solicitanteId)
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Error al cargar ubicaciones compartidas", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun mostrarUbicacionEnMapa(ubicacion: LatLng, usuarioId: String) {
        usersDb.child(usuarioId).child("nombre").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                val nombreUsuario = data.getValue(String::class.java) ?: "Desconocido"
                val inicial = nombreUsuario.firstOrNull()?.toString() ?: ""  // Extraer la inicial del nombre

                // Crear un círculo con la inicial en el centro
                val circleOptions = CircleOptions()
                    .center(ubicacion)
                    .radius(50.0)
                    .strokeWidth(2f)
                    .strokeColor(0xFF0000FF.toInt())
                    .fillColor(0x220000FF)

                mMap.addCircle(circleOptions)

                val circleBitmap = createCircleBitmapWithText(inicial)

                // Crear las opciones del marcador
                val markerOptions = MarkerOptions()
                    .position(ubicacion)
                    .icon(BitmapDescriptorFactory.fromBitmap(circleBitmap))

                // Añadir el marcador al mapa
                val marker = mMap.addMarker(markerOptions)

                // El listener de clic en el marcador se configura en el GoogleMap globalmente
                mMap.setOnMarkerClickListener { clickedMarker ->
                    if (clickedMarker == marker) {
                        showNavigateButton(ubicacion)
                        return@setOnMarkerClickListener true
                    }
                    false
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showNavigateButton(ubicacion: LatLng) {
        // Crear un Intent de navegación con Google Maps
        val intent = Intent(Intent.ACTION_VIEW).apply {
            // URI para Google Maps con la ubicación deseada
            data = Uri.parse("google.navigation:q=${ubicacion.latitude},${ubicacion.longitude}")
            setPackage("com.google.android.apps.maps")
        }

        // Verificar si Google Maps está disponible
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No se puede abrir Google Maps", Toast.LENGTH_SHORT).show()
        }
    }


    private fun createCircleBitmapWithText(text: String): Bitmap {
        val size = 100 // Tamaño del círculo
        val paint = Paint()
        paint.color = Color.BLUE
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
        paint.isAntiAlias = true

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // Dibujar el círculo
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.color = Color.WHITE
        // Dibujar el texto (la inicial) en el círculo
        canvas.drawText(text.uppercase(), size / 2f, size / 2f + paint.textSize / 4, paint)

        return bitmap
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
