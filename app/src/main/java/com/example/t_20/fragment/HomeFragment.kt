package com.example.t_20.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.t_20.R
import com.example.t_20.adapter.ProductAdapter
import com.example.t_20.databinding.FragmentHomeBinding
import com.example.t_20.model.Product
import com.google.android.material.chip.Chip

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()

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
        setupProductsGrid()
        loadSampleProducts()
        setupCategories()
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
                filterProducts(categoryId)
            }
        }

        // Initial filter
        filterProducts("accesorios")
    }

    private fun setupProductsGrid() {
        productAdapter = ProductAdapter()
        binding.recyclerProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
        }
    }

    private fun loadSampleProducts() {
        allProducts = listOf(
            // Accesorios
            Product(1, "Black Ring", 99.00, null, R.drawable.ic_launcher_background, "accesorios"),
            Product(2, "Mate Bracelet", 24.99, null, R.drawable.ic_launcher_background, "accesorios"),
            Product(3, "Military Necklace", 69.99, null, R.drawable.ic_launcher_background, "accesorios"),
            Product(4, "Necklace", 55.75, null, R.drawable.ic_launcher_background, "accesorios"),

            // Camisas
            Product(5, "Camisa Casual", 45.99, null, R.drawable.ic_launcher_background, "camisas"),
            Product(6, "Camisa Formal", 59.99, null, R.drawable.ic_launcher_background, "camisas"),
            Product(7, "Camisa Manga Corta", 35.50, null, R.drawable.ic_launcher_background, "camisas"),
            Product(8, "Camisa Estampada", 42.00, null, R.drawable.ic_launcher_background, "camisas"),

            // Pantalones
            Product(9, "Jean Clásico", 79.99, null, R.drawable.ic_launcher_background, "pantalones"),
            Product(10, "Pantalón Formal", 89.99, null, R.drawable.ic_launcher_background, "pantalones"),
            Product(11, "Jogger Deportivo", 55.00, null, R.drawable.ic_launcher_background, "pantalones"),
            Product(12, "Short Casual", 39.99, null, R.drawable.ic_launcher_background, "pantalones"),

            // Poleras
            Product(13, "Polera Básica", 25.99, null, R.drawable.ic_launcher_background, "poleras"),
            Product(14, "Polera Estampada", 32.99, null, R.drawable.ic_launcher_background, "poleras"),
            Product(15, "Polera Oversize", 38.50, null, R.drawable.ic_launcher_background, "poleras"),
            Product(16, "Polera Deportiva", 29.99, null, R.drawable.ic_launcher_background, "poleras"),

            // Polos
            Product(17, "Polo Clásico", 49.99, null, R.drawable.ic_launcher_background, "polos"),
            Product(18, "Polo Deportivo", 45.00, null, R.drawable.ic_launcher_background, "polos"),
            Product(19, "Polo Slim Fit", 52.99, null, R.drawable.ic_launcher_background, "polos"),
            Product(20, "Polo Casual", 47.50, null, R.drawable.ic_launcher_background, "polos")
        )
    }

    private fun filterProducts(categoryId: String) {
        val filteredProducts = allProducts.filter { it.category == categoryId }
        productAdapter.updateProducts(filteredProducts)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
