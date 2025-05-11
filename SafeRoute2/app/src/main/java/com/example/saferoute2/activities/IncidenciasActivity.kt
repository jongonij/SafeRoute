package com.example.saferoute2.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saferoute2.Adapters.IncidenciaAdapter
import com.example.saferoute2.R
import com.example.saferoute2.data.Views.IncidenciaViewModel
import com.example.saferoute2.data.api.RetrofitServiceFactory
import com.example.saferoute2.data.model.Record
import kotlinx.coroutines.launch

/**
 * Pantalla principal que se encarga de mostrar las incidencias de tráfico en una lista.
 * También notifica al usuario si se detectan nuevas incidencias desde la API.
 */
class IncidenciasActivity : AppCompatActivity() {

    private lateinit var viewModel: IncidenciaViewModel
    private lateinit var adapter: IncidenciaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incidencias)


        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = IncidenciaAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


        viewModel = ViewModelProvider(this)[IncidenciaViewModel::class.java]

        viewModel.incidencias.observe(this) { lista ->
            adapter.submitList(lista)
        }

        lifecycleScope.launch {
            val nuevasIncidencias = obtenerIncidenciasDesdeApi()
            if (nuevasIncidencias.isNotEmpty()) {
                viewModel.actualizarIncidenciasDesdeApi(nuevasIncidencias)
                enviarNotificacion(nuevasIncidencias.size)
            }
        }
    }

    /**
     * Envía una notificación al usuario indicando la cantidad de incidencias nuevas detectadas.
     */
    private fun enviarNotificacion(nuevas: Int) {
        val canalId = "canal_incidencias"
        val manager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                canalId,
                "Incidencias",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(canal)
        }

        val notificacion = NotificationCompat.Builder(this, canalId)
            .setSmallIcon(R.drawable.saferoute_logo_hd)
            .setContentTitle("Atención")
            .setContentText("Hay $nuevas incidencia(s) nueva(s) registradas.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(1, notificacion)
    }

    /**
     * Consulta la API de incidencias y devuelve los datos obtenidos.
     * Si algo falla, captura el error y devuelve una lista vacía.
     */
    private suspend fun obtenerIncidenciasDesdeApi(): List<Record> {
        return try {
            val retrofitService = RetrofitServiceFactory.makeRetrofitService()
            val respuesta = retrofitService.obtenerIncidencias("9323f68f-9c8f-47e1-884c-d6985b957606")
            Log.d("INCIDENCIAS", "Datos obtenidos: ${respuesta.result.records}")
            respuesta.result.records
        } catch (e: Exception) {
            Log.e("INCIDENCIAS", "Fallo al cargar incidencias: ${e.message}", e)
            emptyList()
        }
    }
}
