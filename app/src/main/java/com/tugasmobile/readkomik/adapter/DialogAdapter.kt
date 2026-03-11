package com.tugasmobile.readkomik.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tugasmobile.readkomik.data.Comik
import com.tugasmobile.readkomik.databinding.ItemChapterBinding

class DialogAdapter(
    private val list: List<Comik>,
    private var currentIndex: Int,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<DialogAdapter.VH>() {

    inner class VH(val binding: ItemChapterBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemChapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]

        holder.binding.tvTitle.text = item.judul ?: "Tanpa Judul"

        holder.itemView.isSelected = position == currentIndex

        holder.itemView.setOnClickListener {
            val oldIndex = currentIndex
            currentIndex = position

            notifyItemChanged(oldIndex)
            notifyItemChanged(currentIndex)

            onClick(position)
        }
    }
}