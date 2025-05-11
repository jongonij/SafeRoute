package com.example.saferoute2.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saferoute2.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
/**
 * Actividad para iniciar sesion.
 * Maneja:
 * - Comprueba el email y contraseña usando Firebase
 * - Redirige automáticamente si ya hay sesión iniciada
 * - LLeva a la pantalla para registrarse
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    /**
     * Configuración inicial de la pantalla de inicio de sesion .
     *
     * @param savedInstanceState Estado previo de la pantalla
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
                Toast.makeText(this, "No puede haber campos vacios", Toast.LENGTH_SHORT).show()
            }
        }
    }
    /**
     * Se llama cuando la actividad se hace visible para el usuario.
     * Si ya hay un usuario iniciado, se redirige automáticamente a MainActivity para que no tenga que hacer el login otra vez.
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
