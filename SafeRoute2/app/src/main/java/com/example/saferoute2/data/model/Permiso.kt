package com.example.saferoute2.data.model

data class Permiso(
    var id: String = "",
    var estado: String = "",
    val receptorId: String = "",
    val solicitanteId: String = "",
    var nombreSolicitante: String = ""
)
