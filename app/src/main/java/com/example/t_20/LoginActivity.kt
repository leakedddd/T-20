package com.example.t_20

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.t_20.data.AppDatabase
import com.example.t_20.databinding.ActivityLoginBinding
import java.util.concurrent.Executors

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left + 24,
                systemBars.top + 24,
                systemBars.right + 24,
                systemBars.bottom + 24
            )
            insets
        }

        db = AppDatabase.getInstance(this)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnLogin.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.register_error_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Executors.newSingleThreadExecutor().execute {
                val user = db.userDao().login(email, password)
                runOnUiThread {
                    if (user != null) {
                        val prefs = getSharedPreferences("t20_prefs", MODE_PRIVATE)
                        prefs.edit()
                            .putInt("user_id", user.id)
                            .putString("user_name", user.name)
                            .putString("user_email", user.email)
                            .apply()
                        finish()
                    } else {
                        Toast.makeText(this, getString(R.string.login_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}
