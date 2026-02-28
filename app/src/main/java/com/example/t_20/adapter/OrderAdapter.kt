package com.example.t_20.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.t_20.R
import com.example.t_20.databinding.ItemOrderBinding
import com.example.t_20.model.OrderWithItems
import com.example.t_20.util.ImageHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(
    private var orders: List<OrderWithItems> = emptyList()
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))

    inner class OrderViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(orderWithItems: OrderWithItems) {
            val order = orderWithItems.order
            val items = orderWithItems.items

            binding.apply {
                txtOrderNumber.text = root.context.getString(R.string.order_number, order.id)
                txtOrderDate.text = dateFormat.format(Date(order.date))
                txtOrderStatus.text = order.status
                txtOrderItemsCount.text = root.context.getString(R.string.order_items_count, items.sumOf { it.quantity })
                txtOrderTotal.text = String.format(Locale.US, "$%.2f", order.total)

                // Clear previous items
                layoutOrderItems.removeAllViews()

                // Add product thumbnails (max 4)
                val displayItems = items.take(4)
                for (item in displayItems) {
                    val imageView = ImageView(root.context).apply {
                        layoutParams = LinearLayout.LayoutParams(40.dpToPx(), 40.dpToPx()).apply {
                            marginEnd = 8.dpToPx()
                        }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setBackgroundResource(R.drawable.bg_thumbnail)
                        clipToOutline = true
                    }

                    if (!item.productImageUrl.isNullOrEmpty()) {
                        imageView.load(item.productImageUrl) {
                            placeholder(R.drawable.error404)
                            error(R.drawable.error404)
                            memoryCacheKey(item.productImageUrl)
                        }
                    } else {
                        val imageRes = if (ImageHelper.isValidResourceId(root.context, item.productImageRes)) {
                            item.productImageRes
                        } else {
                            ImageHelper.getImageResForProduct(item.productName)
                        }
                        imageView.setImageResource(imageRes)
                    }

                    layoutOrderItems.addView(imageView)
                }
            }
        }

        private fun Int.dpToPx(): Int {
            return (this * binding.root.context.resources.displayMetrics.density).toInt()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<OrderWithItems>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
