package com.example.saferoute.auth



import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun loginUser(email: String, password: String, callback: (FirebaseUser?, String?) -> Unit) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(mAuth.currentUser, null)
                } else {
                    callback(null, task.exception?.message)
                }
            }
    }

    fun logoutUser() {
        mAuth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return mAuth.currentUser
    }
}
