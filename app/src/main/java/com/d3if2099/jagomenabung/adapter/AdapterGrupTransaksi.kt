package com.d3if2099.jagomenabung.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.d3if2099.jagomenabung.R
import com.d3if2099.jagomenabung.model.Transaksi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdapterGrupTransaksi(private val grupTransaksi: Map<Long, List<Transaksi>>, private val listener : AdapterUserTransaksi.onItemClicklistener) :
    RecyclerView.Adapter<AdapterGrupTransaksi.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tanggalTextView: TextView = itemView.findViewById(R.id.tv_tanggal)
        val transaksiRecyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewItemTransaksi)
        // Tambahan view lainnya
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_transaksi, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (tanggal, transaksi) = grupTransaksi.entries.elementAt(position)

        val formattedDate = formatDate(tanggal)
        holder.tanggalTextView.text = formattedDate

        val transactionAdapter = AdapterUserTransaksi(transaksi, listener)
        holder.transaksiRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }
    }

    override fun getItemCount(): Int {
        return grupTransaksi.size
    }

    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}