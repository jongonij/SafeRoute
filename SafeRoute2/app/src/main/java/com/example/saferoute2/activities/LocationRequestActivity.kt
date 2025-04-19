package com.example.saferoute2.activities

import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saferoute2.R
import com.example.saferoute2.data.model.Permiso
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LocationRequestActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var database: DatabaseReference
    private lateinit var usersDb: DatabaseReference
    private lateinit var miUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_request)

        listView = findViewById(R.id.requests_list)
        database = FirebaseDatabase.getInstance().getReference("permisos")
        usersDb = FirebaseDatabase.getInstance().getReference("usuarios")
        miUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        cargarPermisos()
    }

    private fun cargarPermisos() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val permisos = mutableListOf<Permiso>()

                for (permisoSnapshot in snapshot.children) {
                    val permiso = permisoSnapshot.getValue(Permiso::class.java)
                    permiso?.let {
                        it.id = permisoSnapshot.key ?: ""
                        if (it.receptorId == miUid && it.estado == "PENDIENTE") {
                            permisos.add(it)
                        }
                    }
                }

                // Obtener nombres de los solicitantes
                for (permiso in permisos) {
                    usersDb.child(permiso.solicitanteId).child("nombre")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(data: DataSnapshot) {
                                permiso.nombreSolicitante = data.getValue(String::class.java) ?: permiso.solicitanteId
                                listView.adapter = PermisoAdapter(this@LocationRequestActivity, permisos)
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LocationRequestActivity, "Error al cargar permisos", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
