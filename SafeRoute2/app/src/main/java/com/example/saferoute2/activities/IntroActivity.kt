package com.example.saferoute2.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.saferoute2.R
import com.example.saferoute2.data.Service.LocationUpdateService
import com.example.saferoute2.data.location.ControladorLocalizacion
import com.example.saferoute2.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {

    private lateinit var binding: ActivityIntroBinding
    private lateinit var controladorLocalizacion: ControladorLocalizacion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controladorLocalizacion = ControladorLocalizacion(this)
        val serviceIntent = Intent(this, LocationUpdateService::class.java)
        startService(serviceIntent)
        if (verificarPermisosGPS()) {
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

    private fun verificarPermisosGPS(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun verificarPermisosGPSEnSegundoPlano(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Para Android 9 y versiones anteriores no es necesario
        }
    }
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si se concede el permiso de ubicación en primer plano y en segundo plano
                if (verificarPermisosGPSEnSegundoPlano()) {
                    controladorLocalizacion.iniciarActualizacionUbicacion()
                }
            } else {
                Log.e("Permisos", "El permiso de ubicación no fue concedido.")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}
