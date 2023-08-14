package com.d3if2099.jagomenabung.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.d3if2099.jagomenabung.R
import com.d3if2099.jagomenabung.model.Transaksi
import java.text.NumberFormat
import java.util.*

class AdapterUserTransaksi(private val transaksiList: List<Transaksi>, private val listener: onItemClicklistener): RecyclerView.Adapter<AdapterUserTransaksi.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_transaksi, parent, false)
        return MyViewHolder(itemView)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val transaksi : Transaksi = transaksiList[position]
        holder.judul.text = transaksi.judul
        holder.keterangan.text = transaksi.keterangan
        val saldo = transaksi.jumlahSaldo
        val formatter = NumberFormat.getCurrencyInstance(Locale("in","ID"))
        val saldoformatter = formatter.format(saldo.toString().toDouble()).replace(",00", "")
        holder.saldo.text = saldoformatter
        if (transaksi.kategori == "Pengeluaran"){
            holder.icBackground.setBackgroundResource(R.drawable.rp_pengeluaran)
            holder.saldo.setTextColor(Color.RED)
        } else if (transaksi.kategori == "Pemasukan"){
            holder.icBackground.setBackgroundResource(R.drawable.rp_pemasukan)
            holder.saldo.setTextColor(Color.parseColor("#03A309"))
        }
        holder.show.setOnClickListener { listener.onItemClick(transaksi, it)}
    }

    override fun getItemCount(): Int {
        return transaksiList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val judul : TextView = itemView.findViewById(R.id.tv_judul)
        val keterangan : TextView = itemView.findViewById(R.id.tv_keterangan)
        val saldo : TextView = itemView.findViewById(R.id.tv_saldotransaksi)
        val icBackground: ImageView = itemView.findViewById(R.id.ic_transaksi)
        val show : CardView = itemView.findViewById(R.id.listTransaksi)
    }

    interface onItemClicklistener{
        fun onItemClick(transaksi : Transaksi, v : View)
    }
}