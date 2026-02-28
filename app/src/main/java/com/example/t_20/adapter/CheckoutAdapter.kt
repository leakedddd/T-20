package com.example.t_20.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.t_20.R
import com.example.t_20.databinding.ItemCheckoutProductBinding
import com.example.t_20.model.CartWithProduct
import com.example.t_20.util.ImageHelper
import java.util.Locale

class CheckoutAdapter(
    private var items: List<CartWithProduct> = emptyList()
) : RecyclerView.Adapter<CheckoutAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: ItemCheckoutProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartWithProduct) {
            // Limpiar imagen anterior para evitar caché incorrecta
            binding.imgCheckoutProduct.setImageDrawable(null)

            if (!item.product.imageUrl.isNullOrEmpty()) {
                binding.imgCheckoutProduct.load(item.product.imageUrl) {
                    placeholder(R.drawable.error404)
                    error(R.drawable.error404)
                    memoryCacheKey(item.product.imageUrl)
                }
            } else {
                val imageRes = if (ImageHelper.isValidResourceId(binding.root.context, item.product.imageRes)) {
                    item.product.imageRes
                } else {
                    ImageHelper.getImageResForProduct(item.product.name)
                }
                binding.imgCheckoutProduct.setImageResource(imageRes)
            }
            binding.txtCheckoutProductName.text = "${item.product.name} x${item.cartItem.quantity}"
            binding.txtCheckoutProductPrice.text = String.format(
                Locale.US,
                "$%.2f",
                item.product.price * item.cartItem.quantity
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCheckoutProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<CartWithProduct>) {
        items = newItems
        notifyDataSetChanged()
    }
}
