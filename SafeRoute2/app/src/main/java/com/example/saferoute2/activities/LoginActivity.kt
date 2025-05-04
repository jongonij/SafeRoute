package com.example.saferoute2.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saferoute2.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * Actividad encargada del inicio de sesión de los usuarios.
 * Permite autenticarse con correo electrónico y contraseña mediante Firebase Authentication.
 * También redirige automáticamente al usuario si ya ha iniciado sesión previamente.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    /**
     * Se ejecuta al crear la actividad. Configura el view binding,
     * instancia FirebaseAuth, gestiona el inicio de sesión y la navegación al registro.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.textView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.buttonLogIn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Guardar UID en SharedPreferences
                        val uid = firebaseAuth.currentUser?.uid
                        if (uid != null) {
                            val prefs = getSharedPreferences("SafeRoutePrefs", MODE_PRIVATE)
                            prefs.edit().putString("user_uid", uid).apply()
                        }

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "No se admiten campos vacíos!!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    /**
     * Se ejecuta cuando la actividad se vuelve visible para el usuario.
     * Si ya hay un usuario autenticado, se redirige automáticamente a MainActivity.
     */
    override fun onStart() {
        super.onStart()

        if (firebaseAuth.currentUser != null) {
            // Guardar UID también aquí por si ya estaba logueado
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                val prefs = getSharedPreferences("SafeRoutePrefs", MODE_PRIVATE)
                prefs.edit().putString("user_uid", uid).apply()
            }

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
