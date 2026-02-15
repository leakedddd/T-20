package com.example.t_20

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.t_20.adapter.CheckoutAdapter
import com.example.t_20.data.AppDatabase
import com.example.t_20.databinding.ActivityCheckoutBinding
import java.util.Locale
import java.util.concurrent.Executors

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var db: AppDatabase
    private lateinit var checkoutAdapter: CheckoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
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

        db = AppDatabase.getInstance(this)
        checkoutAdapter = CheckoutAdapter()

        binding.recyclerCheckoutItems.apply {
            layoutManager = LinearLayoutManager(this@CheckoutActivity)
            adapter = checkoutAdapter
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnConfirm.setOnClickListener {
            Executors.newSingleThreadExecutor().execute {
                db.cartDao().clearCart()
                runOnUiThread {
                    Toast.makeText(this, getString(R.string.checkout_success), Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }

        loadCheckoutData()
    }

    private fun loadCheckoutData() {
        Executors.newSingleThreadExecutor().execute {
            val cartItems = db.cartDao().getCartWithProducts()
            val totalItems = db.cartDao().getTotalItems() ?: 0
            val totalPrice = db.cartDao().getTotalPrice() ?: 0.0

            runOnUiThread {
                checkoutAdapter.updateItems(cartItems)
                binding.txtCheckoutSummary.text = String.format(
                    Locale.US,
                    getString(R.string.checkout_items),
                    totalItems,
                    totalPrice
                )
                binding.txtCheckoutTotal.text = String.format(Locale.US, "$%.2f", totalPrice)
            }
        }
    }
}
