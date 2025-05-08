package com.example.saferoute2.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.saferoute2.R
import com.example.saferoute2.data.model.Incidencia
import java.text.DateFormat
import java.util.Date

class IncidenciaAdapter : ListAdapter<Incidencia, IncidenciaAdapter.ViewHolder>(DIFF) {

    object DIFF : DiffUtil.ItemCallback<Incidencia>() {
        override fun areItemsTheSame(oldItem: Incidencia, newItem: Incidencia) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Incidencia, newItem: Incidencia) = oldItem == newItem
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titulo: TextView = view.findViewById(R.id.titulo)
        val descripcion: TextView = view.findViewById(R.id.descripcion)
        val fecha: TextView = view.findViewById(R.id.fecha)
        val gravedad: TextView = view.findViewById(R.id.gravedad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_incidencia, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val incidencia = getItem(position)
        holder.titulo.text = incidencia.titulo
        holder.descripcion.text = incidencia.descripcion
        holder.gravedad.text = incidencia.gravedad
        holder.fecha.text = DateFormat.getDateTimeInstance().format(Date(incidencia.fecha))
    }
}
