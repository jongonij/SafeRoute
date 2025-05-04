package com.example.saferoute2.activities

import android.os.Bundle
import android.view.View
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saferoute2.Adapters.PermisoAdapter
import com.example.saferoute2.Adapters.UsuarioAdapter
import com.example.saferoute2.R
import com.example.saferoute2.data.model.Permiso
import com.example.saferoute2.data.model.Usuario
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * Actividad encargada de gestionar las solicitudes de localización entre usuarios.
 * Permite:
 * - Ver solicitudes entrantes (pendientes o aceptadas).
 * - Ver solicitudes que el usuario ha enviado y han sido aceptadas.
 * - Buscar usuarios y enviar nuevas solicitudes.
 *
 * Usa Firebase Realtime Database para gestionar los permisos y usuarios,
 * y FirebaseAuth para identificar al usuario actual.
 *
 * Adaptadores utilizados:
 * - [PermisoAdapter] para mostrar permisos.
 * - [UsuarioAdapter] para mostrar la lista filtrada de usuarios buscados.
 */
class LocationRequestActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var ubicacionesCompartidasConmigoListView: ListView
    private lateinit var searchView: SearchView
    private lateinit var userListView: ListView
    private lateinit var permisosDb: DatabaseReference
    private lateinit var usersDb: DatabaseReference
    private lateinit var miUid: String
    private val usuarios = mutableListOf<Usuario>()
    private var usuariosFiltrados = mutableListOf<Usuario>()
    private lateinit var acceptedListView: ListView
    private val marcadores = mutableMapOf<String, Marker>() // Initialize the map in the activity

    /**
     * Inicializa la actividad configurando vistas, listeners y carga inicial de datos.
     * Se conecta a Firebase Realtime Database para obtener permisos y usuarios.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_request)

        acceptedListView = findViewById(R.id.accepted_requests_list)
        listView = findViewById(R.id.requests_list)
        searchView = findViewById(R.id.search_view)
        userListView = findViewById(R.id.user_list)
        ubicacionesCompartidasConmigoListView = findViewById(R.id.compartidas_conmigo_list)

        userListView.visibility = View.GONE

        permisosDb = FirebaseDatabase.getInstance().getReference("permisos")
        usersDb = FirebaseDatabase.getInstance().getReference("usuarios")
        miUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        cargarPermisos()
        configurarBuscador()
        cargarSolicitudesCompartidasConmigo()

    }

    /**
     * Carga los permisos desde Firebase y los categoriza entre pendientes y aceptados.
     * Actualiza las listas correspondientes mediante el adaptador [PermisoAdapter].
     * Solo se muestran solicitudes dirigidas al usuario actual.
     */
    private fun cargarPermisos() {
        permisosDb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pendientes = mutableListOf<Permiso>()
                val aceptados = mutableListOf<Permiso>()

                for (permisoSnapshot in snapshot.children) {
                    val permiso = permisoSnapshot.getValue(Permiso::class.java)
                    permiso?.let {
                        it.id = permisoSnapshot.key ?: ""
                        if (it.receptorId == miUid) {
                            when (it.estado) {
                                "PENDIENTE" -> pendientes.add(it)
                                "ACEPTADO" -> aceptados.add(it)
                            }
                        }
                    }
                }
                // Obtener nombres y asignar adaptadores para ambas listas
                cargarNombresYActualizar(pendientes, listView)
                cargarNombresYActualizar(aceptados, acceptedListView, mostrarSoloRechazar = true)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@LocationRequestActivity,
                    "Error al cargar permisos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    /**
     * Asocia a cada permiso el nombre del solicitante obtenido desde Firebase y
     * actualiza el adaptador correspondiente una vez todos han sido cargados.
     *
     * @param permisos Lista de permisos a mostrar.
     * @param listView Vista que mostrará los permisos.
     * @param mostrarSoloRechazar Si es true, solo se mostrará la opción de rechazar (sin aceptar).
     */
    private fun cargarNombresYActualizar(permisos: List<Permiso>, listView: ListView, mostrarSoloRechazar: Boolean = false
    ) {
        val total = permisos.size
        var cargados = 0
        if (permisos.isEmpty()) {
            listView.adapter = null
            return
        }
        for (permiso in permisos) {
            usersDb.child(permiso.solicitanteId).child("nombre")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(data: DataSnapshot) {
                        permiso.nombreSolicitante =
                            data.getValue(String::class.java) ?: permiso.solicitanteId
                        cargados++
                        if (cargados == total) {
                            listView.adapter = PermisoAdapter(
                                this@LocationRequestActivity,
                                permisos,
                                mostrarSoloRechazar,
                                marcadores

                            )
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    /**
     * Carga desde Firebase los permisos que el usuario ha enviado y han sido aceptados.
     * Estos representan las ubicaciones que otros han compartido con él.
     * Se muestra en la lista `ubicacionesCompartidasConmigoListView`.
     */
    private fun cargarSolicitudesCompartidasConmigo() {
        permisosDb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val compartidasConmigo = mutableListOf<Permiso>()
                for (permSnapshot in snapshot.children) {
                    val permiso = permSnapshot.getValue(Permiso::class.java)
                    permiso?.let {
                        it.id = permSnapshot.key ?: ""
                        if (it.solicitanteId == miUid && it.estado == "ACEPTADO") {
                            compartidasConmigo.add(it)
                        }
                    }
                }
                if (compartidasConmigo.isEmpty()) {
                    ubicacionesCompartidasConmigoListView.adapter = null
                    return
                }
                var cargados = 0
                for (permiso in compartidasConmigo) {
                    usersDb.child(permiso.receptorId).child("nombre")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(data: DataSnapshot) {
                                permiso.nombreSolicitante =
                                    data.getValue(String::class.java) ?: permiso.receptorId
                                cargados++
                                if (cargados == compartidasConmigo.size) {
                                    ubicacionesCompartidasConmigoListView.adapter = PermisoAdapter(
                                        this@LocationRequestActivity,
                                        compartidasConmigo,
                                        soloRechazar = true,
                                        marcadores = marcadores // Pasar el mapa de marcadores

                                    )
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    /**
     * Configura el buscador para mostrar usuarios disponibles al enfocar
     * el `SearchView`. Filtra usuarios en tiempo real por nombre.
     * Permite enviar solicitudes a usuarios filtrados.
     */
    private fun configurarBuscador() {
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            userListView.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }

        usersDb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usuarios.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(Usuario::class.java)
                    user?.let {
                        if (it.id != miUid) usuarios.add(it)
                    }
                }

                usuariosFiltrados = usuarios.toMutableList()

                // Inicializamos el adapter solo una vez
                val adapter = UsuarioAdapter(this@LocationRequestActivity, usuariosFiltrados) {
                    enviarSolicitud(it)
                }
                userListView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val texto = newText ?: ""
                usuariosFiltrados.clear()
                usuariosFiltrados.addAll(usuarios.filter {
                    it.nombre.contains(texto, ignoreCase = true)
                })
                (userListView.adapter as BaseAdapter).notifyDataSetChanged()
                return true
            }
        })
    }

    /**
     * Envía una solicitud de permiso de localización al usuario indicado,
     * siempre que no exista ya una solicitud pendiente o aceptada entre ambos.
     *
     * @param usuario Usuario al que se le desea solicitar permiso de ubicación.
     */
    private fun enviarSolicitud(usuario: Usuario) {
        // Verifica si ya existe una solicitud pendiente o aceptada
        permisosDb.orderByChild("solicitanteId").equalTo(miUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var solicitudExiste = false

                    for (permSnapshot in snapshot.children) {
                        val permiso = permSnapshot.getValue(Permiso::class.java)
                        if (permiso != null && permiso.receptorId == usuario.id &&
                            (permiso.estado == "PENDIENTE" || permiso.estado == "ACEPTADO")
                        ) {
                            solicitudExiste = true
                            break
                        }
                    }

                    if (solicitudExiste) {
                        Toast.makeText(
                            this@LocationRequestActivity,
                            "Ya has enviado una solicitud a ${usuario.nombre}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val solicitudRef = permisosDb.push()
                        val nuevoPermiso = Permiso(
                            nombreSolicitante = usuario.nombre,
                            solicitanteId = miUid,
                            receptorId = usuario.id,
                            estado = "PENDIENTE"
                        )

                        solicitudRef.setValue(nuevoPermiso).addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    this@LocationRequestActivity,
                                    "Solicitud enviada a ${usuario.nombre}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@LocationRequestActivity,
                                    "Error al enviar solicitud",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@LocationRequestActivity,
                        "Error al verificar solicitudes existentes",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
