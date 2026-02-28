package com.example.t_20.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.t_20.R
import com.example.t_20.databinding.ItemAdminProductBinding
import com.example.t_20.model.Product
import java.util.Locale

class AdminProductAdapter(
    private var products: List<Product> = emptyList(),
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder>() {

    inner class AdminProductViewHolder(
        private val binding: ItemAdminProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                if (!product.imageUrl.isNullOrEmpty()) {
                    imgProduct.load(product.imageUrl) {
                        placeholder(R.drawable.black_ring)
                        error(R.drawable.black_ring)
                    }
                } else if (product.imageRes != 0) {
                    imgProduct.setImageResource(product.imageRes)
                } else {
                    imgProduct.setImageResource(R.drawable.black_ring)
                }
                txtName.text = product.name
                txtCategory.text = product.category.replaceFirstChar { it.uppercase() }
                txtPrice.text = String.format(Locale.US, "$%.2f", product.price)
                txtStock.text = "Stock: ${product.stock}"

                btnEdit.setOnClickListener { onEditClick(product) }
                btnDelete.setOnClickListener { onDeleteClick(product) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminProductViewHolder {
        val binding = ItemAdminProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
