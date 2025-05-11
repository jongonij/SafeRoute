package com.example.saferoute2.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.saferoute2.R
import com.example.saferoute2.data.Service.LocationUpdateService
import com.example.saferoute2.data.location.ControladorLocalizacion
import com.example.saferoute2.databinding.ActivityIntroBinding

/**
 * Actividad inicial que gestiona:
 * - Configuración de la interfaz edge-to-edge
 * - Verificación y solicitud de permisos de ubicación
 * - Inicio del servicio de actualización de ubicación
 * - Navegación a la actividad de login
 *
 * Esta clase implementa la lógica necesaria para garantizar que la aplicación
 * tenga los permisos requeridos antes de comenzar a rastrear la ubicación.
 */
class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private lateinit var controladorLocalizacion: ControladorLocalizacion

    /**
     * Método principal de inicialización de la actividad.
     *
     * @param savedInstanceState Estado previo de la actividad (puede ser nulo)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración visual edge-to-edge
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        enableEdgeToEdge()

        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controladorLocalizacion = ControladorLocalizacion(this)

        // Inicio del servicio de ubicación persistente
        Intent(this, LocationUpdateService::class.java).also { intent ->
            startService(intent)
            Log.d("IntroActivity", "Servicio de ubicación iniciado")
        }

        // Gestión de permisos con flujo alternativo
        when {
            tieneTodosLosPermisos() -> controladorLocalizacion.iniciarActualizacionUbicacion()
            else -> solicitarPermisosRequeridos()
        }

        binding.btnComenzar.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    /**
     * Verifica si se han concedido todos los permisos necesarios.
     *
     * @return true si todos los permisos están concedidos
     */
    private fun tieneTodosLosPermisos(): Boolean {
        return verificarPermisoPrimerPlano() && verificarPermisoSegundoPlano()
    }

    /**
     * Comprueba el permiso de ubicación en primer plano.
     *
     * @return Estado del permiso ACCESS_FINE_LOCATION
     */
    private fun verificarPermisoPrimerPlano(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Verifica el permiso de ubicación en segundo plano (requerido desde Android 10).
     *
     * @return Estado del permiso o true para versiones anteriores
     */
    private fun verificarPermisoSegundoPlano(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Solicita al usuario los permisos necesarios según la versión de Android.
     * Incluye validación para versiones recientes que requieren permiso explícito
     * para acceso en segundo plano.
     */
    private fun solicitarPermisosRequeridos() {
        val permisosRequeridos = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permisosRequeridos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        ActivityCompat.requestPermissions(
            this,
            permisosRequeridos.toTypedArray(),
            CODIGO_SOLICITUD_PERMISOS
        )
    }

    companion object {
        private const val CODIGO_SOLICITUD_PERMISOS = 1001
    }
}