package com.example.saferoute2.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.saferoute2.R
import com.example.saferoute2.data.model.Permiso
import com.google.firebase.database.FirebaseDatabase

class PermisoAdapter(
    private val context: Context,
    private val permisos: List<Permiso>,
    private val soloRechazar: Boolean = false
) : BaseAdapter() {

    override fun getCount(): Int = permisos.size
    override fun getItem(position: Int): Any = permisos[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val permiso = permisos[position]
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_permiso, parent, false)

        val nombreText = view.findViewById<TextView>(R.id.solicitante_nombre)
        val btnAceptar = view.findViewById<Button>(R.id.btn_aceptar)
        val btnRechazar = view.findViewById<Button>(R.id.btn_rechazar)

        nombreText.text = "Solicitud de: ${permiso.nombreSolicitante}"

        // Ocultar botón Aceptar si ya fue aceptado
        btnAceptar.visibility =
            if (soloRechazar || permiso.estado == "ACEPTADO") View.GONE else View.VISIBLE


        val permisosDb = FirebaseDatabase.getInstance().getReference("permisos").child(permiso.id)

        btnAceptar.setOnClickListener {
            permisosDb.child("estado").setValue("ACEPTADO").addOnSuccessListener {
                permiso.estado = "ACEPTADO" // actualiza estado local
                notifyDataSetChanged() // refresca vista
                Toast.makeText(context, "Solicitud aceptada", Toast.LENGTH_SHORT).show()
            }
        }

        btnRechazar.setOnClickListener {
            if (permiso.estado == "ACEPTADO") {
                permisosDb.removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Solicitud eliminada", Toast.LENGTH_SHORT).show()
                    }
            }
            else {
                permisosDb
                    .child("estado").setValue("RECHAZADO")
                    .addOnSuccessListener {
                        Toast.makeText(context, "Solicitud rechazada", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        return view
    }
}
