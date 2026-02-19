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
import com.example.t_20.model.CartWithProduct
import com.example.t_20.model.Order
import com.example.t_20.model.OrderItem
import java.util.Locale
import java.util.concurrent.Executors

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var db: AppDatabase
    private lateinit var checkoutAdapter: CheckoutAdapter

    private var cartItems: List<CartWithProduct> = emptyList()
    private var totalPrice: Double = 0.0

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
            val address = binding.editAddress.text.toString().trim()
            if (address.isEmpty()) {
                binding.editAddress.error = getString(R.string.checkout_address_hint)
                return@setOnClickListener
            }

            confirmOrder(address)
        }

        loadCheckoutData()
    }

    private fun confirmOrder(address: String) {
        val prefs = getSharedPreferences("t20_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, getString(R.string.login_required), Toast.LENGTH_SHORT).show()
            return
        }

        Executors.newSingleThreadExecutor().execute {
            // Create order
            val order = Order(
                userId = userId,
                total = totalPrice,
                address = address
            )
            val orderId = db.orderDao().insertOrder(order)

            // Create order items
            val orderItems = cartItems.map { cartWithProduct ->
                OrderItem(
                    orderId = orderId.toInt(),
                    productName = cartWithProduct.product.name,
                    productPrice = cartWithProduct.product.price,
                    quantity = cartWithProduct.cartItem.quantity,
                    productImageRes = cartWithProduct.product.imageRes
                )
            }
            db.orderDao().insertOrderItems(orderItems)

            // Clear cart
            db.cartDao().clearCart()

            runOnUiThread {
                Toast.makeText(this, getString(R.string.checkout_success), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun loadCheckoutData() {
        Executors.newSingleThreadExecutor().execute {
            cartItems = db.cartDao().getCartWithProducts()
            val totalItems = db.cartDao().getTotalItems() ?: 0
            totalPrice = db.cartDao().getTotalPrice() ?: 0.0

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
