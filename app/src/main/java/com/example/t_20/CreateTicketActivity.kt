package com.example.t_20

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.t_20.data.AppDatabase
import com.example.t_20.databinding.ActivityCreateTicketBinding
import com.example.t_20.model.Ticket
import java.util.concurrent.Executors

class CreateTicketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTicketBinding
    private lateinit var db: AppDatabase

    private var selectedImageUri: Uri? = null
    private var userId: Int = -1

    private val motivos = listOf(
        "Selecciona un motivo",
        "Pedido dañado",
        "Talla errónea",
        "Se envió producto erróneo",
        "Otro (especifica)"
    )

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imgEvidence.setImageURI(it)
            binding.imgEvidence.visibility = View.VISIBLE
            binding.layoutUploadPlaceholder.visibility = View.GONE

            // Enable description field
            binding.editDescription.isEnabled = true
            binding.txtImageWarning.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateTicketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left + 24,
                systemBars.top + 24,
                systemBars.right + 24,
                systemBars.bottom + 24
            )
            insets
        }

        // Check if user is logged in
        val prefs = getSharedPreferences("t20_prefs", MODE_PRIVATE)
        userId = prefs.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, getString(R.string.ticket_login_required), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        db = AppDatabase.getInstance(this)

        setupSpinner()
        setupListeners()
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, motivos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMotivo.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.cardImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnSubmit.setOnClickListener {
            submitTicket()
        }
    }

    private fun submitTicket() {
        val motivoPosition = binding.spinnerMotivo.selectedItemPosition
        val descripcion = binding.editDescription.text.toString().trim()

        // Validate motivo
        if (motivoPosition == 0) {
            Toast.makeText(this, getString(R.string.ticket_error_motivo), Toast.LENGTH_SHORT).show()
            return
        }

        // Validate image
        if (selectedImageUri == null) {
            Toast.makeText(this, getString(R.string.ticket_error_image), Toast.LENGTH_SHORT).show()
            return
        }

        // Validate description
        if (descripcion.isEmpty()) {
            Toast.makeText(this, getString(R.string.ticket_error_description), Toast.LENGTH_SHORT).show()
            return
        }

        val motivo = motivos[motivoPosition]

        Executors.newSingleThreadExecutor().execute {
            try {
                val ticket = Ticket(
                    userId = userId,
                    motivo = motivo,
                    descripcion = descripcion,
                    imagePath = selectedImageUri.toString()
                )
                val ticketId = db.ticketDao().insert(ticket)

                runOnUiThread {
                    showSuccessDialog(ticketId.toInt())
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        getString(R.string.ticket_error_save),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showSuccessDialog(ticketId: Int) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.ticket_success_title))
            .setMessage(getString(R.string.ticket_success_message, ticketId))
            .setPositiveButton(getString(R.string.ticket_success_ok)) { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
