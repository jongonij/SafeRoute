package com.example.saferoute2.data.repository

import androidx.lifecycle.LiveData
import com.example.saferoute2.data.dao.IncidenciaDao
import com.example.saferoute2.data.model.Incidencia

class IncidenciaRepository(private val incidenciaDao: IncidenciaDao) {

    fun obtenerIncidencias(): LiveData<List<Incidencia>> {
        return incidenciaDao.obtenerTodas()
    }

    // Función para insertar nuevas incidencias
    suspend fun actualizarIncidencias(incidencias: List<Incidencia>) {
        incidenciaDao.eliminarTodas()
        incidenciaDao.insertarTodas(incidencias)
    }
}
