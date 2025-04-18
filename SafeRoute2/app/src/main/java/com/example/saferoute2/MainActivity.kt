package com.example.saferoute2

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.saferoute2.databinding.ActivityIntroBinding
import com.example.saferoute2.databinding.ActivityMainBinding
import com.example.saferoute2.ui.theme.SafeRoute2Theme
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * MainActivity es la actividad principal de la aplicación.
 * Se encarga de manejar la navegación de la aplicación y proporcionar acceso a diferentes vistas.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
//    /**
//     * Configura el menú de navegación inferior
//     */
//    private fun setupBottomMenu() {
//        bottomNavigationView = findViewById(R.id.bottom_navigation)
//        bottomNavigationView.setOnItemSelectedListener { item -> onItemSelectedListener(item) }
//        bottomNavigationView.selectedItemId = R.id.menu_home // Selecciona el home al inicio
//    }
//
//    /**
//     * Maneja la selección de elementos en el menú de navegación inferior.
//     *
//     * @param item Elemento del menú seleccionado.
//     * @return "true" si la selección fue manejada correctamente.
//     */
//    private fun onItemSelectedListener(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.menu_home -> {
//                // Si ya estamos en la pantalla principal, no hacer nada
//                return true
//            }
//            R.id.menu_ranking_local -> {
//                // Ir a la pantalla del ranking local
//                val intent = Intent(this, RankingLocalActivity::class.java)
//                startActivity(intent)
//                return true
//            }
//            R.id.menu_ranking_global -> {
//                // Ir a la pantalla del ranking global
//                val intent = Intent(this, RankingGlobalActivity::class.java)
//                startActivity(intent)
//                return true
//            }
//            else -> throw IllegalArgumentException("Item no implementado: ${item.itemId}")
//        }
//    }
//
//    /**
//     * Navega a la actividad de usuario.
//     */
//    fun goPerfil() {
//        val intent = Intent(this, UserActivity::class.java)
//        startActivity(intent)
//    }
//
//    /**
//     * Método para iniciar una partida según la dificultad seleccionada.
//     *
//     * @param difficulty Dificultad seleccionada.
//     */
//    fun startGame(difficulty: String) {
//        val intent = Intent(this, GameActivity::class.java)
//        intent.putExtra("difficulty", difficulty)
//        startActivity(intent)
//    }
}
