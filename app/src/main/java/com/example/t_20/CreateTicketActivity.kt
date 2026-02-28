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
import androidx.lifecycle.lifecycleScope
import com.example.t_20.data.AppDatabase
import com.example.t_20.data.FirebaseRepository
import com.example.t_20.databinding.ActivityCreateTicketBinding
import com.example.t_20.model.Ticket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateTicketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTicketBinding
    private lateinit var db: AppDatabase
    private val firebaseRepo = FirebaseRepository()

    private var selectedImageUri: Uri? = null
    private var userId: Int = -1
    private var userEmail: String? = null

    private val orderIds = mutableListOf<Int>()
    private val orderLabels = mutableListOf<String>()

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
        userEmail = prefs.getString("user_email", null)

        if (userId == -1) {
            Toast.makeText(this, getString(R.string.ticket_login_required), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        db = AppDatabase.getInstance(this)

        setupMotivoSpinner()
        loadOrders()
        setupListeners()
    }

    private fun setupMotivoSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, motivos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMotivo.adapter = adapter
    }

    private fun loadOrders() {
        lifecycleScope.launch {
            orderLabels.clear()
            orderIds.clear()

            orderLabels.add(getString(R.string.ticket_select_order))
            orderIds.add(-1)

            // Cargar desde Firebase primero
            val firebaseOrders = if (userEmail != null) {
                withContext(Dispatchers.IO) {
                    try {
                        firebaseRepo.getOrders(userEmail!!)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            } else {
                emptyList()
            }

            if (firebaseOrders.isNotEmpty()) {
                firebaseOrders.forEach { (order, _) ->
                    orderLabels.add(getString(R.string.ticket_order_item, order.id, order.total))
                    orderIds.add(order.id)
                }
            } else {
                // Fallback a Room local si no hay en Firebase
                val localOrders = withContext(Dispatchers.IO) { db.orderDao().getOrdersByUser(userId) }
                if (localOrders.isEmpty()) {
                    Toast.makeText(this@CreateTicketActivity, getString(R.string.ticket_no_orders), Toast.LENGTH_LONG).show()
                } else {
                    localOrders.forEach { orderWithItems ->
                        val order = orderWithItems.order
                        orderLabels.add(getString(R.string.ticket_order_item, order.id, order.total))
                        orderIds.add(order.id)
                    }
                }
            }

            val adapter = ArrayAdapter(this@CreateTicketActivity, android.R.layout.simple_spinner_item, orderLabels)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerPedido.adapter = adapter
        }
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
        val pedidoPosition = binding.spinnerPedido.selectedItemPosition
        val descripcion = binding.editDescription.text.toString().trim()

        // Validate motivo
        if (motivoPosition == 0) {
            Toast.makeText(this, getString(R.string.ticket_error_motivo), Toast.LENGTH_SHORT).show()
            return
        }

        // Validate order selection
        if (pedidoPosition == 0 || orderIds.getOrNull(pedidoPosition) == -1) {
            Toast.makeText(this, getString(R.string.ticket_error_order), Toast.LENGTH_SHORT).show()
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
        val orderId = orderIds[pedidoPosition]

        lifecycleScope.launch {
            try {
                val ticket = Ticket(
                    userId = userId,
                    orderId = orderId,
                    motivo = motivo,
                    descripcion = descripcion,
                    imagePath = selectedImageUri.toString()
                )
                val ticketId = withContext(Dispatchers.IO) {
                    val id = db.ticketDao().insert(ticket)
                    userEmail?.let { email ->
                        try { firebaseRepo.saveTicket(email, ticket.copy(id = id.toInt())) } catch (e: Exception) { }
                    }
                    id
                }

                showSuccessDialog(ticketId.toInt())
            } catch (e: Exception) {
                Toast.makeText(
                    this@CreateTicketActivity,
                    getString(R.string.ticket_error_save),
                    Toast.LENGTH_SHORT
                ).show()
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
