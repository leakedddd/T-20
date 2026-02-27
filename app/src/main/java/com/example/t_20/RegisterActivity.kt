package com.example.t_20

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.t_20.data.AppDatabase
import com.example.t_20.data.FirebaseRepository
import com.example.t_20.databinding.ActivityRegisterBinding
import com.example.t_20.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var db: AppDatabase
    private val firebaseRepo = FirebaseRepository()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
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

        binding.btnRegister.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.register_error_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    val firebaseUser = result.user

                    if (firebaseUser != null) {
                        val user = User(name = name, email = email, password = "")
                        val id = withContext(Dispatchers.IO) { db.userDao().register(user) }
                        val userWithId = user.copy(id = id.toInt())

                        withContext(Dispatchers.IO) {
                            try { firebaseRepo.saveUserProfile(userWithId) } catch (e: Exception) { }
                        }

                        Toast.makeText(this@RegisterActivity, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                        val prefs = getSharedPreferences("t20_prefs", MODE_PRIVATE)
                        prefs.edit()
                            .putInt("user_id", id.toInt())
                            .putString("user_name", name)
                            .putString("user_email", email)
                            .apply()
                        finish()
                    }
                } catch (e: Exception) {
                    val message = when {
                        e.message?.contains("email address is already in use") == true ->
                            getString(R.string.register_error_exists)
                        e.message?.contains("weak password") == true ->
                            "La contraseña debe tener al menos 6 caracteres"
                        e.message?.contains("invalid email") == true ->
                            "Email inválido"
                        else -> "Error: ${e.message}"
                    }
                    Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.txtLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
