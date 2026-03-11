package com.tugasmobile.readkomik.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tugasmobile.readkomik.data.FolderComik
import com.tugasmobile.readkomik.databinding.ItemFolderBinding

class FolderAdapter (
    private val listFolder: List<FolderComik>,
    private val onClick: (FolderComik, Int) -> Unit
) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemFolderBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = listFolder[position]

        holder.binding.txtFolderName.text = folder.folderName
        holder.binding.txtTotalPdf.text = "${folder.totalPdf} PDF"
        holder.binding.root.setOnClickListener {
            onClick(folder, position)
        }
    }

    override fun getItemCount() = listFolder.size
}
