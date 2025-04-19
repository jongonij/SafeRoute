package com.example.saferoute2.data.firebase

import android.util.Log
import com.example.saferoute2.data.model.Ubicacion
import com.example.saferoute2.data.model.Usuario
import com.google.firebase.database.FirebaseDatabase

object FirebaseDatabaseManager {

    private val database = FirebaseDatabase.getInstance().reference

    fun crearUsuarioEnBD(uid: String, nombre: String, email: String) {
        val ubicacionInicial = Ubicacion(0.0, 0.0, System.currentTimeMillis())
        val contactos = emptyMap<String, Boolean>()

        val nuevoUsuario = Usuario(
            id = uid,
            nombre = nombre,
            email = email,
            ubicacionActual = ubicacionInicial,
            contactosPermitidos = contactos
        )
        database.child("usuarios").child(uid).setValue(nuevoUsuario)
            .addOnSuccessListener {
                Log.d("FirebaseDB", "Usuario creado correctamente en Realtime DB")
            }
            .addOnFailureListener {
                Log.e("FirebaseDB", "Error al crear usuario en DB: ${it.message}")
            }
    }
}
