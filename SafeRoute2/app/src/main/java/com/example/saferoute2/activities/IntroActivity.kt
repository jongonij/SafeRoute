package com.example.saferoute2.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputBinding
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.saferoute2.MainActivity
import com.example.saferoute2.R
import com.example.saferoute2.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {
    private lateinit var binding: ActivityIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
     binding.apply {
         btnComenzar.setOnClickListener{
             startActivity(Intent(this@IntroActivity,MainActivity::class.java))
         }
     }
    }
}