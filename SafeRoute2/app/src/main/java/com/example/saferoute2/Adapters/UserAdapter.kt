package com.example.saferoute2.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.saferoute2.R
import com.example.saferoute2.data.model.Usuario
import android.content.Context
import android.widget.*


/**
 * Adaptador para mostrar una lista de usuarios en una vista de lista.
 *
 * @param context Contexto de la actividad o fragmento donde se utiliza el adaptador.
 * @param usuarios Lista de objetos Usuario a mostrar.
 * @param onClick Función que se ejecuta al hacer clic en un elemento de la lista.
 */
class UsuarioAdapter(
    private val context: Context,
    private val usuarios: List<Usuario>,
    private val onClick: (Usuario) -> Unit
) : BaseAdapter() {

    /**
     * Método que devuelve la cantidad de elementos en la lista de usuarios.
     *
     * @return Cantidad de usuarios.
     */
    override fun getCount() = usuarios.size
    /**
     * Método que devuelve el elemento en la posición especificada.
     *
     * @param position Posición del elemento a devolver.
     * @return Objeto Usuario en la posición especificada.
     */
    override fun getItem(position: Int) = usuarios[position]
    /**
     * Método que devuelve el ID del elemento en la posición especificada.
     *
     * @param position Posición del elemento cuyo ID se desea obtener.
     * @return ID del elemento en la posición especificada.
     */
    override fun getItemId(position: Int) = position.toLong()
    /**
     * Método que crea y devuelve la vista para un elemento en la lista de usuarios.
     *
     * @param position Posición del elemento en la lista.
     * @param convertView Vista reciclada (si existe).
     * @param parent Grupo padre al que pertenece la vista.
     * @return Vista creada o reciclada para el elemento en la posición especificada.
     */

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val usuario = usuarios[position]
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = usuario.nombre
        view.setOnClickListener { onClick(usuario) }
        return view
    }
}

