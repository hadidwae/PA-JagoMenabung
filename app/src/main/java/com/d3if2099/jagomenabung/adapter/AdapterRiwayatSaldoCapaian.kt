package com.d3if2099.jagomenabung.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.d3if2099.jagomenabung.R
import com.d3if2099.jagomenabung.model.RiwayatInputSaldo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.ArrayList
import java.util.Locale

class AdapterRiwayatSaldoCapaian(private val riwayatList: ArrayList<RiwayatInputSaldo>, private val deleteListener: (RiwayatInputSaldo) -> Unit): RecyclerView.Adapter<AdapterRiwayatSaldoCapaian.MyViewHolder>() {
    private val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_riwayat_input, parent, false)
        return MyViewHolder(itemView)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val riwayat : RiwayatInputSaldo = riwayatList[position]
        val saldo = riwayat.saldo
        val formatter = NumberFormat.getCurrencyInstance(Locale("in","ID"))
        val saldoformatter = formatter.format(saldo.toString().toDouble()).replace(",00", "")
        holder.saldo.text = saldoformatter
        holder.tanggal.text = riwayat.tanggal
        val id = riwayat.idCapaian.toString()
        val capaianRef = FirebaseDatabase.getInstance().reference.child("Capaian").child(firebaseUser.uid).child(id)

        capaianRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val kunci = snapshot.child("kunci").getValue(Boolean::class.java) ?: false
                if (kunci){
                    holder.btnHapus.visibility = View.GONE
                } else {
                    holder.btnHapus.visibility = View.VISIBLE
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        holder.btnHapus.setOnClickListener {
            deleteListener(riwayat)
        }
    }

    override fun getItemCount(): Int {
        return riwayatList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tanggal : TextView = itemView.findViewById(R.id.tanggalInput)
        val saldo : TextView = itemView.findViewById(R.id.saldoInput)
        val btnHapus : ImageButton = itemView.findViewById(R.id.imgBtnDelete)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<RiwayatInputSaldo>) {
        riwayatList.clear()
        riwayatList.addAll(data)
        notifyDataSetChanged()
    }
}