package com.example.t_20.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.t_20.R
import com.example.t_20.adapter.ProductAdapter
import com.example.t_20.data.AppDatabase
import com.example.t_20.data.FirebaseRepository
import com.example.t_20.databinding.FragmentHomeBinding
import com.example.t_20.model.CartItem
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private lateinit var db: AppDatabase
    private val firebaseRepo = FirebaseRepository()

    private var currentCategory = "accesorios"
    private var currentQuery = ""

    private val categories = listOf(
        "Accesorios" to "accesorios",
        "Camisas" to "camisas",
        "Pantalones" to "pantalones",
        "Poleras" to "poleras",
        "Polos" to "polos"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext())
        setupProductsGrid()
        setupCategories()
        setupSearch()
    }

    private fun setupSearch() {
        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentQuery = s?.toString()?.trim() ?: ""
                filterProducts(currentCategory)
            }
        })
    }

    private fun setupCategories() {
        categories.forEachIndexed { index, (displayName, categoryId) ->
            val chip = Chip(requireContext()).apply {
                text = displayName
                tag = categoryId
                isCheckable = true
                isChecked = index == 0
                setChipBackgroundColorResource(R.color.chip_bg_selector)
                setTextColor(resources.getColorStateList(R.color.chip_text_selector, null))
                chipStrokeColor = resources.getColorStateList(R.color.chip_stroke, null)
                chipStrokeWidth = resources.getDimension(R.dimen.chip_stroke_width)
                chipCornerRadius = resources.getDimension(R.dimen.chip_corner_radius)
                setEnsureMinTouchTargetSize(false)
            }
            binding.chipGroupCategories.addView(chip)
        }

        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChip = group.findViewById<Chip>(checkedIds.first())
                val categoryId = selectedChip?.tag as? String ?: "accesorios"
                currentCategory = categoryId
                filterProducts(categoryId)
            }
        }

        filterProducts("accesorios")
    }

    override fun onResume() {
        super.onResume()
        filterProducts(currentCategory)
    }

    private fun setupProductsGrid() {
        productAdapter = ProductAdapter { product ->
            viewLifecycleOwner.lifecycleScope.launch {
                val cartItem = withContext(Dispatchers.IO) { db.cartDao().getCartItem(product.id) }
                val currentQty = cartItem?.quantity ?: 0
                if (currentQty >= product.stock) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.stock_error, product.stock),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val newCartItem = CartItem(productId = product.id, quantity = currentQty + 1)
                    withContext(Dispatchers.IO) {
                        db.cartDao().insert(newCartItem)
                        val prefs = requireContext().getSharedPreferences("t20_prefs", 0)
                        val userEmail = prefs.getString("user_email", null)
                        if (userEmail != null) {
                            try { firebaseRepo.saveCartItem(userEmail, newCartItem) } catch (e: Exception) { }
                        }
                    }
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.added_to_cart),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        binding.recyclerProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
        }
    }

    private fun filterProducts(categoryId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val products = withContext(Dispatchers.IO) {
                if (currentQuery.isEmpty()) {
                    db.productDao().getByCategory(categoryId)
                } else {
                    db.productDao().searchByCategory(categoryId, currentQuery)
                }
            }
            productAdapter.updateProducts(products)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
