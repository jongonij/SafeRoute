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

/**
 * Adaptador para mostrar una lista de objetos [Incidencia] en un RecyclerView.
 * Utiliza ListAdapter para aprovechar DiffUtil y mejorar el rendimiento.
 */
class IncidenciaAdapter : ListAdapter<Incidencia, IncidenciaAdapter.ViewHolder>(DIFF) {

    /**
     * Comparador que determina cuándo una incidencia ha cambiado.
     * Esto permite que solo se actualicen los elementos necesarios.
     */
    object DIFF : DiffUtil.ItemCallback<Incidencia>() {

        /**
         * Comprueba si dos incidencias son la misma, comparando sus IDs.
         *
         * @param oldItem Elemento anterior.
         * @param newItem Elemento nuevo.
         * @return true si son el mismo elemento, false en caso contrario.
         */
        override fun areItemsTheSame(oldItem: Incidencia, newItem: Incidencia): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * Comprueba si el contenido de dos incidencias es idéntico.
         *
         * @param oldItem Elemento anterior.
         * @param newItem Elemento nuevo.
         * @return true si todo el contenido es igual, false si hay diferencias.
         */
        override fun areContentsTheSame(oldItem: Incidencia, newItem: Incidencia): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * ViewHolder que contiene la vista de un solo ítem de incidencia.
     *
     * @param view Vista inflada del layout correspondiente al ítem.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titulo: TextView = view.findViewById(R.id.titulo)
        val descripcion: TextView = view.findViewById(R.id.descripcion)
        val fecha: TextView = view.findViewById(R.id.fecha)
        val gravedad: TextView = view.findViewById(R.id.gravedad)
    }

    /**
     * Crea una nueva vista para mostrar una incidencia.
     *
     * @param parent Contenedor padre donde se colocará la vista.
     * @param viewType Tipo de vista, no usado en este caso.
     * @return Nuevo ViewHolder con la vista lista para usarse.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incidencia, parent, false)
        return ViewHolder(vista)
    }

    /**
     * Asigna los datos de una incidencia a la vista del ViewHolder.
     *
     * @param holder ViewHolder que se va a rellenar.
     * @param position Posición del elemento actual en la lista.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val incidencia = getItem(position)
        holder.titulo.text = incidencia.titulo
        holder.descripcion.text = incidencia.descripcion
        holder.gravedad.text = incidencia.gravedad
        holder.fecha.text = DateFormat.getDateTimeInstance().format(Date(incidencia.fecha))
    }
}
