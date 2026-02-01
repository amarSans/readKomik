package com.tugasmobile.readkomik.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tugasmobile.readkomik.data.database.Comik
import com.tugasmobile.readkomik.databinding.ItemPdfBinding

class PdfAdapter(
    private val listPdf: List<Comik>,
    private val onClick: (Comik,Int) -> Unit
) : RecyclerView.Adapter<PdfAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPdfBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPdfBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comik = listPdf[position]

        holder.binding.tvNamaPdf.text = comik.judul
        if(comik.totalHalaman>0){
            val percent=(comik.progress*100)/comik.totalHalaman
            holder.binding.progressBaca.max=100
            holder.binding.progressBaca.progress=percent

            holder.binding.tvProgressText.text="${comik.totalHalaman} halaman"
        }else{
            holder.binding.progressBaca.progress= 0
            holder.binding.tvProgressText.text="Belum dibaca"
        }
        holder.binding.root.setOnClickListener {
            onClick(comik, position)
        }
    }

    override fun getItemCount() = listPdf.size
}
