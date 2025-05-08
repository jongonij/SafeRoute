package com.example.saferoute2.data.localDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.saferoute2.data.dao.IncidenciaDao
import com.example.saferoute2.data.model.Incidencia
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
@Database(entities = [Incidencia::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun incidenciaDao(): IncidenciaDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        @OptIn(InternalCoroutinesApi::class)
        fun obtenerInstancia(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mi_basedatos.db"
                ).build().also { instancia = it }
            }
        }
    }
}
