package com.example.t_20.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.dispose
import coil.load
import coil.request.CachePolicy
import com.example.t_20.R
import com.example.t_20.databinding.ItemProductBinding
import com.example.t_20.model.Product
import com.example.t_20.util.ImageHelper
import java.util.Locale

class ProductAdapter(
    private var products: List<Product> = emptyList(),
    private val onAddToCart: (Product) -> Unit = {}
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                // Cancelar cualquier carga pendiente y limpiar
                imgProduct.dispose()
                imgProduct.setImageDrawable(null)
                imgProduct.tag = product.id

                if (!product.imageUrl.isNullOrEmpty()) {
                    imgProduct.load(product.imageUrl) {
                        placeholder(R.drawable.error404)
                        error(R.drawable.error404)
                        memoryCachePolicy(CachePolicy.DISABLED)
                        diskCachePolicy(CachePolicy.DISABLED)
                    }
                } else {
                    val imageRes = if (ImageHelper.isValidResourceId(root.context, product.imageRes)) {
                        product.imageRes
                    } else {
                        ImageHelper.getImageResForProduct(product.name)
                    }
                    imgProduct.setImageResource(imageRes)
                }
                txtProductName.text = product.name
                txtProductPrice.text = String.format(Locale.US, "%.2f", product.price)

                btnAddCart.setOnClickListener {
                    onAddToCart(product)
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
