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
import com.google.android.material.tabs.TabLayout

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()

    private val categories = listOf("All", "Women", "Men", "Kids", "Accessories")

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
        setupCategories()
        setupProductsGrid()
        loadSampleProducts()
    }

    private fun setupCategories() {
        categories.forEach { category ->
            binding.tabCategories.addTab(
                binding.tabCategories.newTab().setText(category)
            )
        }

        binding.tabCategories.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val category = tab?.text?.toString() ?: "All"
                filterProducts(category)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
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
            Product(1, "Elegant Summer Dress", 29.99, 49.99, R.drawable.ic_launcher_background, "Women"),
            Product(2, "Classic Denim Jacket", 45.99, 79.99, R.drawable.ic_launcher_background, "Women"),
            Product(3, "Casual Cotton T-Shirt", 15.99, 24.99, R.drawable.ic_launcher_background, "Men"),
            Product(4, "Slim Fit Jeans", 39.99, 59.99, R.drawable.ic_launcher_background, "Men"),
            Product(5, "Kids Cartoon Hoodie", 22.99, 34.99, R.drawable.ic_launcher_background, "Kids"),
            Product(6, "Children's Sneakers", 28.99, 44.99, R.drawable.ic_launcher_background, "Kids"),
            Product(7, "Leather Handbag", 55.99, 89.99, R.drawable.ic_launcher_background, "Accessories"),
            Product(8, "Fashion Sunglasses", 18.99, 29.99, R.drawable.ic_launcher_background, "Accessories"),
            Product(9, "Floral Maxi Skirt", 32.99, 54.99, R.drawable.ic_launcher_background, "Women"),
            Product(10, "Men's Polo Shirt", 24.99, 39.99, R.drawable.ic_launcher_background, "Men"),
            Product(11, "Kids Pajama Set", 19.99, 29.99, R.drawable.ic_launcher_background, "Kids"),
            Product(12, "Silver Necklace", 35.99, 59.99, R.drawable.ic_launcher_background, "Accessories")
        )
        productAdapter.updateProducts(allProducts)
    }

    private fun filterProducts(category: String) {
        val filteredProducts = if (category == "All") {
            allProducts
        } else {
            allProducts.filter { it.category == category }
        }
        productAdapter.updateProducts(filteredProducts)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
