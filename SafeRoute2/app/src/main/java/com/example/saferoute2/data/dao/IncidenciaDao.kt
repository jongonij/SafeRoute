package com.example.saferoute2.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.saferoute2.data.model.Incidencia

@Dao
interface IncidenciaDao {

    @Query("SELECT * FROM incidencias ORDER BY fecha DESC")
    fun obtenerTodas(): LiveData<List<Incidencia>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(incidencias: List<Incidencia>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(incidencia: Incidencia)

    @Delete
    suspend fun eliminar(incidencia: Incidencia)

    @Query("DELETE FROM incidencias")
    suspend fun eliminarTodas()
}
