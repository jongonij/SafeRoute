package com.example.saferoute2.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.saferoute2.R
import com.example.saferoute2.data.model.Permiso
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.FirebaseDatabase

/**
 * Adaptador para mostrar una lista de permisos en una vista de lista.
 *
 * @param context Contexto de la actividad o fragmento donde se utiliza el adaptador.
 * @param permisos Lista de objetos Permiso a mostrar.
 * @param soloRechazar Indica si solo se deben mostrar los botones de rechazo.
 * @param marcadores Mapa que relaciona los IDs de permisos con marcadores en el mapa.
 */
class PermisoAdapter(
    private val context: Context,
    private val permisos: List<Permiso>,
    private val soloRechazar: Boolean = false,
    private val marcadores: MutableMap<String, Marker> // Mapa de ID de permiso a marcador

) : BaseAdapter() {
    /**
     * Método que devuelve la cantidad de elementos en la lista de permisos.
     *
     * @return Cantidad de permisos.
     */
    override fun getCount(): Int = permisos.size
    /**
     * Método que devuelve el elemento en la posición especificada.
     *
     * @param position Posición del elemento a devolver.
     * @return Objeto Permiso en la posición especificada.
     */
    override fun getItem(position: Int): Any = permisos[position]
    /**
     * Método que devuelve el ID del elemento en la posición especificada.
     *
     * @param position Posición del elemento cuyo ID se desea obtener.
     * @return ID del elemento en la posición especificada.
     */
    override fun getItemId(position: Int): Long = position.toLong()
    /**
     * Método que crea y devuelve la vista para un elemento en la lista de permisos.
     *
     * @param position Posición del elemento en la lista.
     * @param convertView Vista reciclada (si existe).
     * @param parent Grupo padre al que pertenece la vista.
     * @return Vista creada o reciclada para el elemento en la posición especificada.
     */

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val permiso = permisos[position]
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_permiso, parent, false)

        val nombreText = view.findViewById<TextView>(R.id.solicitante_nombre)
        val btnAceptar = view.findViewById<Button>(R.id.btn_aceptar)
        val btnRechazar = view.findViewById<Button>(R.id.btn_rechazar)

        nombreText.text = "Nombre de usuario: ${permiso.nombreSolicitante}"

        btnAceptar.visibility =
            if (soloRechazar || permiso.estado == "ACEPTADO") View.GONE else View.VISIBLE

        val permisosDb = FirebaseDatabase.getInstance().getReference("permisos").child(permiso.id)

        btnAceptar.setOnClickListener {
            permisosDb.child("estado").setValue("ACEPTADO").addOnSuccessListener {
                permiso.estado = "ACEPTADO"
                notifyDataSetChanged()
                Toast.makeText(context, "Solicitud aceptada", Toast.LENGTH_SHORT).show()

                // Eliminar marcador del mapa
                marcadores[permiso.id]?.remove()
                marcadores.remove(permiso.id)
            }
        }

        btnRechazar.setOnClickListener {
            if (permiso.estado == "ACEPTADO") {
                permisosDb.removeValue().addOnSuccessListener {
                    Toast.makeText(context, "Solicitud eliminada", Toast.LENGTH_SHORT).show()

                    // Eliminar marcador del mapa
                    marcadores[permiso.id]?.remove()
                    marcadores.remove(permiso.id)
                }
            } else {
                permisosDb.child("estado").setValue("RECHAZADO").addOnSuccessListener {
                    Toast.makeText(context, "Solicitud rechazada", Toast.LENGTH_SHORT).show()

                    // Eliminar marcador del mapa
                    marcadores[permiso.id]?.remove()
                    marcadores.remove(permiso.id)
                }
            }
        }

        return view
    }
}
