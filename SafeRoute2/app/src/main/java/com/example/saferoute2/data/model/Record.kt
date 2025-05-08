package com.example.saferoute2.data.model

data class Record(
    val Afeccion: String,
    val Carretera: String,
    val Categoria: String,
    val Coord_X_en_EPSG_25830: String,
    val Coord_Y_en_EPSG_25830: String,
    val Fecha_incidencia: String,
    val Gravedad: String,
    val Otros_datos: String,
    val PK: String,
    val Tipo: String,
    val Titulo: String,
    val Ubicacion: String,
    val Ultima_actualizacion: String,
    val _id: Int
)