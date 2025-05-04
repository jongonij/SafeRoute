package com.example.saferoute2.data.firebase

import android.util.Log
import com.example.saferoute2.data.model.Ubicacion
import com.example.saferoute2.data.model.Usuario
import com.google.firebase.database.FirebaseDatabase


/**
 * Clase para gestionar la base de datos Firebase Realtime Database.
 * Proporciona métodos para crear y gestionar usuarios en la base de datos.
 */
object FirebaseDatabaseManager {

    private val database = FirebaseDatabase.getInstance().reference

    /**
     * Crea un nuevo usuario en la base de datos Firebase Realtime Database.
     *
     * @param uid El ID único del usuario.
     * @param nombre El nombre del usuario.
     * @param email El correo electrónico del usuario.
     */
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
