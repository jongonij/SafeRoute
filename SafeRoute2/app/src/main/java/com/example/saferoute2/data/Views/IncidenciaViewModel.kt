package com.example.saferoute2.data.Views

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.saferoute2.data.localDatabase.AppDatabase
import com.example.saferoute2.data.model.Incidencia
import com.example.saferoute2.data.model.Record
import com.example.saferoute2.data.repository.IncidenciaRepository
import kotlinx.coroutines.launch

class IncidenciaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: IncidenciaRepository = IncidenciaRepository(AppDatabase.obtenerInstancia(application).incidenciaDao())
    val incidencias: LiveData<List<Incidencia>> = repository.obtenerIncidencias()

    fun actualizarIncidenciasDesdeApi(nuevas: List<Record>) {
        val entidades = nuevas.map { record ->
            Incidencia(
                id = 0, // ID autogenerado por la base de datos
                titulo = record.Titulo,
                descripcion = record.Ubicacion,
                gravedad = record.Gravedad,
                afectacion = record.Afeccion,
                fecha = record.Fecha_incidencia
            )
        }

        viewModelScope.launch {
            repository.actualizarIncidencias(entidades)
        }
    }

}
