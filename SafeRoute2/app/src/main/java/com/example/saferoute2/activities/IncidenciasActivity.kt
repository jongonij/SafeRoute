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

        viewModel.incidencias.observe(this) {
            adapter.submitList(it)
        }

        // Llamada periódica simulada (reemplaza con WorkManager para producción)
        lifecycleScope.launch {

                val nuevas = obtenerIncidenciasDesdeApi()
                if (nuevas.isNotEmpty()) {
                    viewModel.actualizarIncidenciasDesdeApi(nuevas)
                    enviarNotificacion(nuevas.size)
                }

        }
    }

    private fun enviarNotificacion(nuevas: Int) {
        val canalId = "canal_incidencias"
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(canalId, "Incidencias", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(canal)
        }

        val notificacion = NotificationCompat.Builder(this, canalId)
            .setSmallIcon(R.drawable.saferoute_logo_hd)
            .setContentTitle("Nuevas incidencias")
            .setContentText("Se detectaron $nuevas nuevas incidencias.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(1, notificacion)
    }

    private suspend fun obtenerIncidenciasDesdeApi(): List<Record> {
        return try {
            val retrofitService = RetrofitServiceFactory.makeRetrofitService()
            val response = retrofitService.obtenerIncidencias("9323f68f-9c8f-47e1-884c-d6985b957606")
            Log.d("INCIDENCIAS", "MOSTRAR INCIDENCIAS: ${response.result.records}")
            println(response.result.records)
            response.result.records
        } catch (e: Exception) {
            Log.e("INCIDENCIAS", "Error al obtener incidencias: ${e.message}", e)
            emptyList()
        }
    }


}
