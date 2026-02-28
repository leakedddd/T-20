package com.example.t_20.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.example.t_20.R
import com.example.t_20.databinding.ItemCartBinding
import com.example.t_20.model.CartWithProduct
import com.example.t_20.util.ImageHelper
import java.util.Locale

class CartAdapter(
    private var items: List<CartWithProduct> = emptyList(),
    private val onQuantityChanged: (productId: Int, newQuantity: Int) -> Unit,
    private val onRemove: (productId: Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(
        private val binding: ItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartWithProduct) {
            val product = item.product
            val cartItem = item.cartItem

            binding.apply {
                // Limpiar imagen anterior para evitar caché incorrecta
                imgCartProduct.setImageDrawable(null)

                if (!product.imageUrl.isNullOrEmpty()) {
                    imgCartProduct.load(product.imageUrl) {
                        placeholder(R.drawable.error404)
                        error(R.drawable.error404)
                        memoryCachePolicy(CachePolicy.DISABLED)
                    }
                } else {
                    val imageRes = if (ImageHelper.isValidResourceId(root.context, product.imageRes)) {
                        product.imageRes
                    } else {
                        ImageHelper.getImageResForProduct(product.name)
                    }
                    imgCartProduct.setImageResource(imageRes)
                }
                txtCartName.text = product.name
                txtCartPrice.text = String.format(Locale.US, "$%.2f", product.price)
                txtQuantity.text = cartItem.quantity.toString()

                btnDecrease.setOnClickListener {
                    if (cartItem.quantity > 1) {
                        onQuantityChanged(product.id, cartItem.quantity - 1)
                    } else {
                        onRemove(product.id)
                    }
                }

                btnIncrease.setOnClickListener {
                    if (cartItem.quantity >= product.stock) {
                        Toast.makeText(
                            root.context,
                            root.context.getString(R.string.stock_error, product.stock),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        onQuantityChanged(product.id, cartItem.quantity + 1)
                    }
                }

                btnRemove.setOnClickListener {
                    onRemove(product.id)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<CartWithProduct>) {
        items = newItems
        notifyDataSetChanged()
    }
}
