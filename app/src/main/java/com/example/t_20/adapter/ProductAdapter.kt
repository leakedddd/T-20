package com.example.t_20.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.t_20.databinding.ItemProductBinding
import com.example.t_20.model.Product
import java.util.Locale

class ProductAdapter(
    private var products: List<Product> = emptyList()
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                imgProduct.setImageResource(product.imageRes)
                txtProductName.text = product.name
                txtProductPrice.text = String.format(Locale.US, "$%.2f", product.price)

                if (product.originalPrice != null && product.originalPrice > product.price) {
                    txtProductOriginalPrice.visibility = View.VISIBLE
                    txtProductOriginalPrice.text = String.format(Locale.US, "$%.2f", product.originalPrice)
                    txtProductOriginalPrice.paintFlags =
                        txtProductOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    txtProductOriginalPrice.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
