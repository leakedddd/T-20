package com.example.t_20.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.t_20.databinding.ItemFaqBinding
import com.example.t_20.model.Faq

class FaqAdapter(
    private val faqs: List<Faq>
) : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    private var expandedPosition: Int = -1

    inner class FaqViewHolder(
        private val binding: ItemFaqBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(faq: Faq, isExpanded: Boolean) {
            binding.txtQuestion.text = faq.question
            binding.txtAnswer.text = faq.answer

            binding.txtAnswer.visibility = if (isExpanded) View.VISIBLE else View.GONE

            val rotation = if (isExpanded) 45f else 0f
            binding.txtIcon.rotation = rotation

            binding.layoutQuestion.setOnClickListener {
                val previousExpanded = expandedPosition
                expandedPosition = if (isExpanded) -1 else adapterPosition

                if (previousExpanded != -1) {
                    notifyItemChanged(previousExpanded)
                }
                if (expandedPosition != -1) {
                    notifyItemChanged(expandedPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val binding = ItemFaqBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FaqViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        holder.bind(faqs[position], position == expandedPosition)
    }

    override fun getItemCount(): Int = faqs.size
}
