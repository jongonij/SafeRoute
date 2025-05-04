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
 * Actividad de introducción que se muestra al iniciar la aplicación.
 * Inicia el servicio de actualización de ubicación en segundo plano,
 * solicita permisos de ubicación y redirige al usuario al login.
 */
class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private lateinit var controladorLocalizacion: ControladorLocalizacion

    /**
     * Método llamado cuando la actividad se crea.
     * Se inicializa el binding, se arranca el servicio de ubicación y se solicitan permisos si es necesario.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad (si lo hubiera).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        enableEdgeToEdge()
        binding = ActivityIntroBinding.inflate(layoutInflater)


        setContentView(binding.root)

        controladorLocalizacion = ControladorLocalizacion(this)
        val serviceIntent = Intent(this, LocationUpdateService::class.java)
        startService(serviceIntent)
        if (verificarPermisosGPS() && verificarPermisosGPSEnSegundoPlano()) {
            controladorLocalizacion.iniciarActualizacionUbicacion()
        } else {
            pedirPermisosGPS()
        }

        binding.apply {
            btnComenzar.setOnClickListener {
                startActivity(Intent(this@IntroActivity, LoginActivity::class.java))
            }
        }
    }

    /**
     * Verifica si los permisos de ubicación en primer plano están concedidos.
     *
     * @return `true` si el permiso ACCESS_FINE_LOCATION está concedido, `false` en caso contrario.
     */
    private fun verificarPermisosGPS(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Verifica si los permisos de ubicación en segundo plano están concedidos (solo en Android 10+).
     *
     * @return `true` si el permiso ACCESS_BACKGROUND_LOCATION está concedido o si la versión de Android es inferior a Q.
     */
    private fun verificarPermisosGPSEnSegundoPlano(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Para Android 9 y versiones anteriores no es necesario
        }
    }

    /**
     * Solicita los permisos de ubicación necesarios (en primer y segundo plano si aplica).
     * Llama a `ActivityCompat.requestPermissions`.
     */
    private fun pedirPermisosGPS() {
        val permisos = mutableListOf<String>()

        // Solicitar permisos de ubicación en primer plano
        permisos.add(Manifest.permission.ACCESS_FINE_LOCATION)

        // Si estamos en Android 10 o superior, solicitar permisos en segundo plano también
        //El SDK_INT comprueba la version de android del dispositivo y el Q es la referancia a la version 10 de android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        ActivityCompat.requestPermissions(
            this, permisos.toTypedArray(), 1001
        )
    }

}
