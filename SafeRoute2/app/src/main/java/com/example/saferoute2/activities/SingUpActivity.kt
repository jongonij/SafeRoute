package com.example.saferoute2.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saferoute2.data.firebase.FirebaseDatabaseManager
import com.example.saferoute2.databinding.ActivitySingupBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * Actividad encargada del registro de nuevos usuarios.
 * Permite crear una cuenta mediante correo electrónico y contraseña utilizando Firebase Authentication.
 * También redirige al usuario a la actividad de inicio de sesión tras un registro exitoso.
 */
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingupBinding
    private lateinit var firebaseAuth: FirebaseAuth

    /**
     * Se ejecuta al crear la actividad. Configura el view binding,
     * instancia FirebaseAuth y gestiona el registro de nuevos usuarios.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySingupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.textViewSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.buttonSignUp.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val contra = binding.passET.text.toString()
            val nombre = binding.nameEt.text.toString();
            val confirmPass = binding.confirmPassEt.text.toString()

            if (email.isNotEmpty() && contra.isNotEmpty() && confirmPass.isNotEmpty() && nombre.isNotEmpty()) {
                if (contra == confirmPass) {

                    firebaseAuth.createUserWithEmailAndPassword(email, contra)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val user = it.result?.user
                                val uid = user?.uid ?: return@addOnCompleteListener
                                FirebaseDatabaseManager.crearUsuarioEnBD(uid, nombre, email)
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Contraseña no coincide", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No se admiten campos vacios!!", Toast.LENGTH_SHORT).show()

            }
        }
    }
}