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
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.PolyUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Actividad principal que muestra un mapa, maneja permisos, obtiene la ubicación del usuario
 * y visualiza la ubicación de otros usuarios con quienes se han compartido permisos.
 */
class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var permisosDb: DatabaseReference
    private lateinit var usersDb: DatabaseReference
    private lateinit var miUid: String
    private val httpClient = OkHttpClient()
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private var rutaActual: Polyline? = null
    private var marcadorActual: Marker? = null
    private var markers = mutableMapOf<String, Marker>()
    private var currentDestination: LatLng? = null
    private lateinit var btnCancelNavigation: Button
    private lateinit var btnGoogleMaps: Button
    private val refreshHandler = android.os.Handler()
    private val refreshInterval: Long = 15000 // 15 segundos
    private val refreshRunnable = object : Runnable {
        override fun run() {
            cargarUbicacionesCompartidas()
            refreshHandler.postDelayed(this, refreshInterval)
        }
    }
    private val marcadoresUsuarios = mutableMapOf<String, Marker>()
    private val circulosUsuarios = mutableMapOf<String, Circle>()




    /**
     * Inicializa la actividad, configura el mapa, botones y listeners.
     */
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        refreshHandler.postDelayed(refreshRunnable, refreshInterval)



        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        firebaseAuth = FirebaseAuth.getInstance()
        permisosDb = FirebaseDatabase.getInstance().getReference("permisos")
        usersDb = FirebaseDatabase.getInstance().getReference("usuarios")
        miUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        btnCancelNavigation = findViewById(R.id.btn_cancel_navigation)
        btnGoogleMaps = findViewById(R.id.btn_google_maps)
        btnGoogleMaps.setOnClickListener {
            currentDestination?.let { destino ->
                abrirRutaEnGoogleMaps(destino)
            } ?: run {
                Toast.makeText(this, "Selecciona un destino primero", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelNavigation.setOnClickListener {
            removeNavigationRoute()
            btnCancelNavigation.visibility = View.GONE
            btnGoogleMaps.visibility = View.GONE
        }

        cargarUbicacionesCompartidas()

    }


    /**
     * Maneja el resultado de la solicitud de permisos de ubicación.
     *
     * @param requestCode Código de solicitud.
     * @param permissions Array de permisos solicitados.
     * @param grantResults Resultados de los permisos.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, obtener la ubicación
                getCurrentLocationFromFirebase()
            } else {
                // Si el permiso es denegado, mostramos un mensaje y damos la opción de ir a la configuración
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                openLocationSettings()
            }
        }
    }

    /**
     * Abre la configuración de ubicación de la aplicación en caso de que el permiso sea denegado.
     */
    private fun openLocationSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = android.net.Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    /**
     * Obtiene la ubicación actual del usuario desde Firebase y la muestra en el mapa.
     */
    private fun getCurrentLocationFromFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        usersDb.child(uid).child("ubicacionActual")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val latitud = snapshot.child("latitud").getValue(Double::class.java)
                    val longitud = snapshot.child("longitud").getValue(Double::class.java)
                    if (latitud != null && longitud != null) {
                        val currentLocation = LatLng(latitud, longitud)
                        mMap.addMarker(
                            MarkerOptions().position(currentLocation).title("Tu Ubicación Actual")
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "No se pudo obtener la ubicación actual",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al obtener la ubicación",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    /**
     * Se ejecuta cuando el mapa está listo. Habilita la ubicación si se tiene permiso.
     *
     * @param googleMap Instancia del mapa de Google.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Verifica si se otorgó el permiso antes de acceder a la ubicación
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Carga las ubicaciones compartidas por otros usuarios que hayan aceptado compartir su ubicación.
     */
    private fun cargarUbicacionesCompartidas() {
        permisosDb.orderByChild("solicitanteId").equalTo(miUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (permisoSnapshot in snapshot.children) {
                        val permiso = permisoSnapshot.getValue(Permiso::class.java)
                        if (permiso != null && permiso.estado == "ACEPTADO") {
                            // Obtener la ubicación compartida de este permiso
                            usersDb.child(permiso.receptorId).child("ubicacionActual")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(data: DataSnapshot) {
                                        val latitud =
                                            data.child("latitud").getValue(Double::class.java)
                                        val longitud =
                                            data.child("longitud").getValue(Double::class.java)
                                        if (latitud != null && longitud != null) {
                                            val ubicacion = LatLng(latitud, longitud)
                                            mostrarUbicacionEnMapa(ubicacion, permiso.receptorId)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al cargar ubicaciones compartidas",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    /**
     * Muestra en el mapa la ubicación de un usuario junto a su inicial.
     *
     * @param ubicacion Coordenadas de la ubicación.
     * @param usuarioId ID del usuario para identificar su marcador.
     */
    private fun mostrarUbicacionEnMapa(ubicacion: LatLng, usuarioId: String) {
        usersDb.child(usuarioId).child("nombre")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("PotentialBehaviorOverride")
                override fun onDataChange(data: DataSnapshot) {
                    val nombreUsuario = data.getValue(String::class.java) ?: "Desconocido"
                    val inicial = nombreUsuario.firstOrNull()?.toString() ?: ""

                    // Eliminar marcador anterior si existe
                    marcadoresUsuarios[usuarioId]?.remove()
                    marcadoresUsuarios.remove(usuarioId)

                    // Eliminar círculo anterior si existe
                    circulosUsuarios[usuarioId]?.remove()
                    circulosUsuarios.remove(usuarioId)

                    // Crear un nuevo círculo
                    val circleOptions = CircleOptions()
                        .center(ubicacion)
                        .radius(50.0)
                        .strokeWidth(2f)
                        .strokeColor(0xFF0000FF.toInt())
                        .fillColor(0x220000FF)
                    val circle = mMap.addCircle(circleOptions)
                    circulosUsuarios[usuarioId] = circle

                    // Crear un nuevo marcador con la inicial
                    val circleBitmap = crearCirculoBitmapConTexto(inicial)
                    val markerOptions = MarkerOptions()
                        .position(ubicacion)
                        .icon(BitmapDescriptorFactory.fromBitmap(circleBitmap))
                    val marker = mMap.addMarker(markerOptions)

                    if (marker != null) {
                        marcadoresUsuarios[usuarioId] = marker
                    }

                    // Listener de clic
                    mMap.setOnMarkerClickListener { clickedMarker ->
                        if (clickedMarker == marker) {
                            if (btnCancelNavigation.visibility == View.GONE) {
                                btnCancelNavigation.visibility = View.VISIBLE
                                btnGoogleMaps.visibility = View.VISIBLE
                            }
                            if (currentDestination != ubicacion) {
                                currentDestination = ubicacion
                                showNavigateButton(ubicacion)
                            } else {
                                removeNavigationRoute()
                                btnCancelNavigation.visibility = View.GONE
                                btnGoogleMaps.visibility = View.GONE
                            }
                            return@setOnMarkerClickListener true
                        }
                        false
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }


    /**
     * Abre Google Maps con una ruta desde la ubicación actual hasta el destino.
     *
     * @param destino Coordenadas del destino.
     */
    private fun abrirRutaEnGoogleMaps(destino: LatLng?) {
        if (destino == null) {
            Toast.makeText(this, "Destino no válido", Toast.LENGTH_SHORT).show()
            return
        }
        val uri =
            Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${destino.latitude},${destino.longitude}&travelmode=driving")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps") // Asegura que se abra con Google Maps
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Google Maps no está instalado", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Elimina la ruta de navegación y marcador del destino actual.
     */
    private fun removeNavigationRoute() {
        rutaActual?.remove()
        marcadorActual?.remove()

        rutaActual = null
        marcadorActual = null
        currentDestination = null

    }

    /**
     * Muestra el botón para navegar hacia un destino y obtiene la ruta usando la API de Google Directions.
     *
     * @param destino Coordenadas de destino.
     */
    private fun showNavigateButton(destino: LatLng) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        usersDb.child(uid).child("ubicacionActual")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val latitud = snapshot.child("latitud").getValue(Double::class.java)
                    val longitud = snapshot.child("longitud").getValue(Double::class.java)
                    if (latitud != null && longitud != null) {
                        val origen = LatLng(latitud, longitud)
                        obtenerRutaDesdeDirectionsAPI(origen, destino)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "No se pudo obtener tu ubicación actual",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al obtener la ubicación",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    /**
     * Obtiene una ruta entre dos puntos utilizando la API de Google Directions.
     *
     * @param origen Coordenadas de origen.
     * @param destino Coordenadas de destino.
     */
    private fun obtenerRutaDesdeDirectionsAPI(origen: LatLng, destino: LatLng) {
        val apiKey =
            getString(R.string.google_maps_key) // asegúrate de tenerla en res/values/strings.xml
        val url =
            "https://maps.googleapis.com/maps/api/directions/json?" + "origin=${origen.latitude},${origen.longitude}&" +
                    "destination=${destino.latitude},${destino.longitude}&" + "key=$apiKey"
        Thread {
            try {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                val json = response.body?.string()
                val jsonObject = JSONObject(json!!)
                val routes = jsonObject.getJSONArray("routes")
                if (routes.length() > 0) {
                    val polyline = routes.getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points")
                    val points = PolyUtil.decode(polyline)
                    runOnUiThread {
                        removeNavigationRoute()
                        currentDestination = destino
                        rutaActual = mMap.addPolyline(
                            com.google.android.gms.maps.model.PolylineOptions().addAll(points)
                                .color(Color.BLUE).width(10f)
                        )
                        marcadorActual =
                            mMap.addMarker(MarkerOptions().position(destino).title("Destino"))
                        // Establecer la cámara en el origen y destino
                        val builder = LatLngBounds.Builder()
                        builder.include(origen)
                        builder.include(destino)
                        val bounds = builder.build()
                        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50)
                        mMap.animateCamera(cameraUpdate)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "No se encontró ruta", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error al obtener la ruta", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    /**
     * Crea un bitmap circular con un texto en el centro.
     *
     * @param text Texto a dibujar (por ejemplo, una inicial).
     * @return Imagen de tipo Bitmap con el círculo y texto.
     */
    private fun crearCirculoBitmapConTexto(text: String): Bitmap {
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

    /**
     * Infla el menú superior de opciones.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Maneja los eventos del menú superior.
     */

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

    /**
     * Verifica si el usuario está autenticado y lanza el servicio de ubicación.
     */
    @RequiresApi(Build.VERSION_CODES.O)
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

    /**
     * Inicia el servicio de actualización de ubicación en primer plano.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startLocationUpdateService() {
        val serviceIntent = Intent(this, LocationUpdateService::class.java)
        Log.d("LocationService", "Servicio de ubicación en primer plano iniciado")
        startForegroundService(serviceIntent)
    }
    override fun onDestroy() {
        super.onDestroy()
        refreshHandler.removeCallbacks(refreshRunnable)
    }

}
