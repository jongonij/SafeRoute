package com.example.saferoute2.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.saferoute2.R
import com.example.saferoute2.data.Service.LocationUpdateService
import com.example.saferoute2.data.location.ControladorLocalizacion
import com.example.saferoute2.databinding.ActivityIntroBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * Primera pantalla que ve el usuario al abrir la app.
 * Se encarga de:
 * - Poner la pantalla bonita (modo edge-to-edge)
 * - Pedir permisos de ubicación (si no los tiene)
 * - Arrancar el servicio que sigue tu posición
 * - Llevarte al login cuando tocas el botón
 */
class IntroActivity : AppCompatActivity() {

    // Esto es para acceder a los elementos de la pantalla
    private var _binding: ActivityIntroBinding? = null
    private val binding get() = _binding!!

    private lateinit var jefeDeUbicacion: ControladorLocalizacion
    /**
     * Metodo que se inicializa cuando la se crea la actividad.
     *
     * @param savedInstanceState Estado anterior de la actividad (puede ser nulo)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        enableEdgeToEdge()

        _binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        jefeDeUbicacion = ControladorLocalizacion(this)

        // Arrancamos el servicio que seguirá nuestra ubicación
        try {
            val servicio = Intent(this, LocationUpdateService::class.java)
            startService(servicio)
            Log.i("IntroActivity", "Servicio de ubicación lanzado")
        } catch (e: Exception) {
            Log.e("IntroActivity", "Error al arrancar el servicio", e)
        }

        // Comprobamos permisos
        if (tienePermisosDeUbicacion()) {
            jefeDeUbicacion.iniciarActualizacionUbicacion()
        } else {
            pedirPermisos()
        }

        // Cuando tocas el botón "Comenzar"
        binding.btnComenzar.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
             finish()
        }
        // Mostrar u ocultar botón de cerrar sesión
        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario != null) {
            binding.btnCerrarSesion.visibility = View.VISIBLE
            binding.btnCerrarSesion.setOnClickListener {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        } else {
            binding.btnCerrarSesion.visibility = View.GONE
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    /**
     * ¿Tenemos todos los permisos que necesitamos?
     */
    private fun tienePermisosDeUbicacion(): Boolean {
        return puedeUbicarEnPrimerPlano() &&
                (puedeUbicarEnSegundoPlano() || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
    }

    /**
     * Permiso para saber dónde está el usuario cuando la app está abierta
     */
    private fun puedeUbicarEnPrimerPlano(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Permiso para seguir al usuario incluso cuando la app está en segundo plano
     * (Solo necesario a partir de Android 10)
     */
    private fun puedeUbicarEnSegundoPlano(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // En versiones viejas no hace falta
        }
    }

    /**
     * Muestra el diálogo para pedir los permisos necesarios
     */
    private fun pedirPermisos() {
        val permisos = ArrayList<String>()
        permisos.add(Manifest.permission.ACCESS_FINE_LOCATION)

        // Solo pedimos el de segundo plano si es Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        ActivityCompat.requestPermissions(
            this,
            permisos.toTypedArray(),
            CODIGO_DE_PERMISOS
        )
    }

    companion object {
        // Número aleatorio para identificar la respuesta de los permisos
        private const val CODIGO_DE_PERMISOS = 1234
    }
}