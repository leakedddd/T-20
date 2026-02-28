package com.example.t_20.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.dispose
import coil.load
import coil.request.CachePolicy
import com.example.t_20.R
import com.example.t_20.databinding.ItemAdminProductBinding
import com.example.t_20.model.Product
import com.example.t_20.util.ImageHelper
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
                // Cancelar cualquier carga pendiente y limpiar
                imgProduct.dispose()
                imgProduct.setImageDrawable(null)

                // Usar tag para verificar que la imagen corresponde al producto
                imgProduct.tag = product.id

                if (!product.imageUrl.isNullOrEmpty()) {
                    val productId = product.id
                    imgProduct.load(product.imageUrl) {
                        placeholder(R.drawable.error404)
                        error(R.drawable.error404)
                        memoryCachePolicy(CachePolicy.DISABLED)
                        listener(
                            onSuccess = { _, result ->
                                // Verificar que el tag siga siendo el mismo producto
                                if (imgProduct.tag == productId) {
                                    imgProduct.setImageDrawable(result.drawable)
                                }
                            }
                        )
                    }
                } else {
                    val imageRes = if (ImageHelper.isValidResourceId(root.context, product.imageRes)) {
                        product.imageRes
                    } else {
                        ImageHelper.getImageResForProduct(product.name)
                    }
                    imgProduct.setImageResource(imageRes)
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
