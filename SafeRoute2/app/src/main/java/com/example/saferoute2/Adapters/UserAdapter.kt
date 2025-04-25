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

class UsuarioAdapter(
    private val context: Context,
    private val usuarios: List<Usuario>,
    private val onClick: (Usuario) -> Unit
) : BaseAdapter() {

    override fun getCount() = usuarios.size
    override fun getItem(position: Int) = usuarios[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val usuario = usuarios[position]
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = usuario.nombre
        view.setOnClickListener { onClick(usuario) }
        return view
    }
}

