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
import com.example.t_20.databinding.ActivityLoginBinding
import com.example.t_20.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: AppDatabase
    private val firebaseRepo = FirebaseRepository()
    private val auth = FirebaseAuth.getInstance()

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

            lifecycleScope.launch {
                try {
                    val result = auth.signInWithEmailAndPassword(email, password).await()
                    val firebaseUser = result.user

                    if (firebaseUser != null) {
                        var userProfile = withContext(Dispatchers.IO) {
                            try { firebaseRepo.getUserByEmail(email) } catch (e: Exception) { null }
                        }

                        val userName = userProfile?.name ?: firebaseUser.displayName ?: "Usuario"
                        val userId = userProfile?.id ?: 0

                        if (userProfile == null) {
                            val newUser = User(name = userName, email = email, password = "")
                            val id = withContext(Dispatchers.IO) { db.userDao().register(newUser) }
                            userProfile = newUser.copy(id = id.toInt())
                        } else {
                            withContext(Dispatchers.IO) {
                                if (db.userDao().getByEmail(email) == null) {
                                    db.userDao().register(userProfile!!)
                                }
                            }
                        }

                        val prefs = getSharedPreferences("t20_prefs", MODE_PRIVATE)
                        prefs.edit()
                            .putInt("user_id", userProfile!!.id)
                            .putString("user_name", userProfile.name)
                            .putString("user_email", email)
                            .apply()
                        finish()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, getString(R.string.login_error), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}
