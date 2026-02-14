package com.example.t_20.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.t_20.R
import com.example.t_20.adapter.ProductAdapter
import com.example.t_20.data.AppDatabase
import com.example.t_20.databinding.FragmentHomeBinding
import com.example.t_20.model.Product
import com.google.android.material.chip.Chip
import java.util.concurrent.Executors

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private lateinit var db: AppDatabase

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

        // Initial load
        filterProducts("accesorios")
    }

    private fun setupProductsGrid() {
        productAdapter = ProductAdapter()
        binding.recyclerProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
        }
    }

    private fun filterProducts(categoryId: String) {
        Executors.newSingleThreadExecutor().execute {
            val products = db.productDao().getByCategory(categoryId)
            activity?.runOnUiThread {
                productAdapter.updateProducts(products)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
