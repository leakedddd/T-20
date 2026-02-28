package com.example.t_20

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.t_20.adapter.CheckoutAdapter
import com.example.t_20.data.AppDatabase
import com.example.t_20.data.FirebaseRepository
import com.example.t_20.databinding.ActivityCheckoutBinding
import com.example.t_20.model.CartWithProduct
import com.example.t_20.model.Order
import com.example.t_20.model.OrderItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var db: AppDatabase
    private lateinit var checkoutAdapter: CheckoutAdapter
    private val firebaseRepo = FirebaseRepository()

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
        val userEmail = prefs.getString("user_email", null)

        if (userId == -1) {
            Toast.makeText(this, getString(R.string.login_required), Toast.LENGTH_SHORT).show()
            return
        }

        // Deshabilitar botón para prevenir múltiples clicks
        binding.btnConfirm.isEnabled = false
        binding.btnConfirm.text = "Procesando..."

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val order = Order(
                    userId = userId,
                    total = totalPrice,
                    address = address
                )
                val orderId = db.orderDao().insertOrder(order)

                val orderItems = cartItems.map { cartWithProduct ->
                    OrderItem(
                        orderId = orderId.toInt(),
                        productName = cartWithProduct.product.name,
                        productPrice = cartWithProduct.product.price,
                        quantity = cartWithProduct.cartItem.quantity,
                        productImageRes = cartWithProduct.product.imageRes,
                        productImageUrl = cartWithProduct.product.imageUrl
                    )
                }
                db.orderDao().insertOrderItems(orderItems)

                // Reducir stock de cada producto comprado
                cartItems.forEach { cartWithProduct ->
                    val product = cartWithProduct.product
                    val quantityBought = cartWithProduct.cartItem.quantity
                    val newStock = (product.stock - quantityBought).coerceAtLeast(0)
                    db.productDao().updateStock(product.id, newStock)
                }

                // Sincronizar productos actualizados con Firebase
                try {
                    val updatedProducts = db.productDao().getAll()
                    firebaseRepo.syncProducts(updatedProducts)
                } catch (e: Exception) {
                    android.util.Log.e("CheckoutActivity", "Error syncing products: ${e.message}", e)
                }

                if (userEmail != null) {
                    try {
                        firebaseRepo.saveOrder(userEmail, order.copy(id = orderId.toInt()), orderItems)
                        firebaseRepo.clearCart(userEmail)
                        android.util.Log.d("CheckoutActivity", "Order saved to Firebase for $userEmail")
                    } catch (e: Exception) {
                        android.util.Log.e("CheckoutActivity", "Firebase error: ${e.message}", e)
                    }
                } else {
                    android.util.Log.e("CheckoutActivity", "userEmail is NULL - cannot save to Firebase")
                }

                db.cartDao().clearCart()
            }

            Toast.makeText(this@CheckoutActivity, getString(R.string.checkout_success), Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun loadCheckoutData() {
        lifecycleScope.launch {
            val (items, totalItems, price) = withContext(Dispatchers.IO) {
                Triple(
                    db.cartDao().getCartWithProducts(),
                    db.cartDao().getTotalItems() ?: 0,
                    db.cartDao().getTotalPrice() ?: 0.0
                )
            }
            cartItems = items
            totalPrice = price
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
