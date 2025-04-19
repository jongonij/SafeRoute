package com.example.saferoute2.data.model

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val ubicacionActual: Ubicacion? = null,
    val contactosPermitidos: Map<String, Boolean> = emptyMap()
)