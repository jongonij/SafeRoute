package com.example.saferoute2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "incidencias")
data class Incidencia(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val gravedad: String,
    val afectacion: String,
    val fecha: String
)