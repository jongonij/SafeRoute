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
/**
 * Este ViewModel es el que gestiona la lógica de recibir las incidencias.
 * Se encarga de obtener y actualizar las incidencias desde el repositorio.
 *
 * @param application La aplicación en la que se ejecuta este ViewModel.
 */
class IncidenciaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: IncidenciaRepository = IncidenciaRepository(AppDatabase.obtenerInstancia(application).incidenciaDao())
    val incidencias: LiveData<List<Incidencia>> = repository.obtenerIncidencias()

    /**
     * Actualiza las incidencias en la base de datos local a partir de una lista de registros
     * obtenidos desde la API , se le llama desde IncidenciasActivity.
     *
     * Convierte los objetos [Record] en entidades [Incidencia], y luego actualiza los datos
     * en la base de datos utilizando una corrutina.
     *
     * @param nuevas Lista de registros obtenidos desde la API.
     */
    fun actualizarIncidenciasDesdeApi(nuevas: List<Record>) {
        val entidades = nuevas.map { record ->
            Incidencia(
                id = 0, // El id se autogenera
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
