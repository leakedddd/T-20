package com.example.t_20.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
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
                // Limpiar imagen anterior para evitar caché incorrecta
                imgProduct.setImageDrawable(null)

                if (!product.imageUrl.isNullOrEmpty()) {
                    imgProduct.load(product.imageUrl) {
                        placeholder(R.drawable.error404)
                        error(R.drawable.error404)
                        memoryCacheKey(product.imageUrl)
                    }
                } else {
                    // Usar ImageHelper para obtener el drawable correcto por nombre
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
