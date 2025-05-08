package com.example.saferoute2.data.repository

import androidx.lifecycle.LiveData
import com.example.saferoute2.data.dao.IncidenciaDao
import com.example.saferoute2.data.model.Incidencia

class IncidenciaRepository(private val incidenciaDao: IncidenciaDao) {

    // Devuelve el LiveData de las incidencias almacenadas en la base de datos
    fun obtenerIncidencias(): LiveData<List<Incidencia>> {
        return incidenciaDao.obtenerTodas()  // Llamada al DAO para obtener las incidencias
    }

    // Función para insertar nuevas incidencias
    suspend fun actualizarIncidencias(incidencias: List<Incidencia>) {
        incidenciaDao.eliminarTodas()  // Elimina todas las incidencias antes de insertar las nuevas
        incidenciaDao.insertarTodas(incidencias)
    }
}
