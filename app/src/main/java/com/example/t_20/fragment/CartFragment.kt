package com.example.t_20.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.t_20.CheckoutActivity
import com.example.t_20.LoginActivity
import com.example.t_20.R
import com.example.t_20.adapter.CartAdapter
import com.example.t_20.data.AppDatabase
import com.example.t_20.databinding.FragmentCartBinding
import java.util.Locale
import java.util.concurrent.Executors

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartAdapter: CartAdapter
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext())
        setupCartList()
        setupPayButton()
    }

    override fun onResume() {
        super.onResume()
        loadCart()
    }

    private fun setupCartList() {
        cartAdapter = CartAdapter(
            onQuantityChanged = { productId, newQuantity ->
                Executors.newSingleThreadExecutor().execute {
                    db.cartDao().updateQuantity(productId, newQuantity)
                    loadCart()
                }
            },
            onRemove = { productId ->
                Executors.newSingleThreadExecutor().execute {
                    db.cartDao().remove(productId)
                    loadCart()
                }
            }
        )
        binding.recyclerCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun setupPayButton() {
        binding.btnPay.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("t20_prefs", 0)
            val userId = prefs.getInt("user_id", -1)
            if (userId == -1) {
                Toast.makeText(requireContext(), getString(R.string.login_required), Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            } else {
                startActivity(Intent(requireContext(), CheckoutActivity::class.java))
            }
        }
    }

    private fun loadCart() {
        Executors.newSingleThreadExecutor().execute {
            val cartItems = db.cartDao().getCartWithProducts()
            val totalItems = db.cartDao().getTotalItems() ?: 0
            val totalPrice = db.cartDao().getTotalPrice() ?: 0.0

            activity?.runOnUiThread {
                if (cartItems.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.scrollCart.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.scrollCart.visibility = View.VISIBLE
                    cartAdapter.updateItems(cartItems)
                    binding.txtProductCount.text = getString(R.string.cart_products, totalItems)
                    binding.txtSubtotalValue.text = String.format(Locale.US, "$%.2f", totalPrice)
                    binding.txtTotalValue.text = String.format(Locale.US, "$%.2f", totalPrice)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
