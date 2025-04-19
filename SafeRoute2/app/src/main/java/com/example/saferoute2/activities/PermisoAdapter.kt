package com.example.saferoute2.activities

import com.example.saferoute2.data.model.Permiso
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.saferoute2.R
import com.google.firebase.database.FirebaseDatabase

class PermisoAdapter(private val context: Context, private val permisos: List<Permiso>) : BaseAdapter() {
    override fun getCount(): Int = permisos.size
    override fun getItem(position: Int): Any = permisos[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val permiso = permisos[position]
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_permiso, parent, false)

        val nombreText = view.findViewById<TextView>(R.id.solicitante_nombre)
        val btnAceptar = view.findViewById<Button>(R.id.btn_aceptar)
        val btnRechazar = view.findViewById<Button>(R.id.btn_rechazar)

        nombreText.text = "Solicitud de: ${permiso.nombreSolicitante}"

        val permisosDb = FirebaseDatabase.getInstance().getReference("permisos").child(permiso.id)

        btnAceptar.setOnClickListener {
            permisosDb.child("estado").setValue("ACEPTADO")
            Toast.makeText(context, "Solicitud aceptada", Toast.LENGTH_SHORT).show()
        }

        btnRechazar.setOnClickListener {
            permisosDb.child("estado").setValue("RECHAZADO")
            Toast.makeText(context, "Solicitud rechazada", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
