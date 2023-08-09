package com.d3if2099.jagomenabung.adapter

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.d3if2099.jagomenabung.R
import com.d3if2099.jagomenabung.model.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AdapterUserTarget(private val targetList: ArrayList<Target>, private var optionsMenuClickListener: OptionsMenuClickListener): RecyclerView.Adapter<AdapterUserTarget.MyViewHolder>() {
    private val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_target, parent, false)
        return MyViewHolder(itemView)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item: Target = targetList[position]
        holder.judul.text = item.judul
        val mulai = item.tanggalMulai!! / 1000
        val akhir = item.tanggalBerakhir!! / 1000
        val tanggalMulai = timestampToDate(mulai)
        val tanggalBerakhir = timestampToDate(akhir)
        holder.tanggalMulai.text = tanggalMulai
        holder.tanggalAkhir.text = tanggalBerakhir
        val saldo = item.jumlahSaldo
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val saldoformatterTarget = formatter.format(saldo.toString().toDouble())
        holder.saldoTarget.text = saldoformatterTarget.toString()


        val tm = item.tanggalMulai
        val ta = item.tanggalBerakhir
        val database = FirebaseDatabase.getInstance()
        val totalSaldoReference = database.reference.child("TotalSaldo").child(firebaseUser.uid)

        totalSaldoReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(totalSaldoSnapshot: DataSnapshot) {
                if (totalSaldoSnapshot.exists()) {
                    // Mengakses data di dalam snapshot dan menghitung total saldo berdasarkan rentang tanggal
                    var totalSaldo = 0
                    for (childSnapshot in totalSaldoSnapshot.children) {
                        val timestamp = childSnapshot.key?.toLongOrNull()
                        if (timestamp != null && timestamp >= tm && timestamp <= ta) {
                            val saldoData = childSnapshot.child("saldo").getValue(Int::class.java)
                            saldoData?.let {
                                totalSaldo += it
                                val saldoformatterTerkumpul = formatter.format(totalSaldo.toString().toDouble())

                                if(totalSaldo < saldo!!){
                                    holder.tvProgress.setText(R.string.capaian_belum)
                                    holder.tvProgress.setTextColor(Color.RED)
                                    holder.saldoTerkumpul.text = saldoformatterTerkumpul.toString()
                                }else {
                                    holder.tvProgress.setText(R.string.capaian)
                                    holder.tvProgress.setTextColor(Color.parseColor("#03A309"))
                                    holder.saldoTerkumpul.text = saldoformatterTarget.toString()
                                }
                                holder.progressBar.max = saldo
                                holder.progressBar.progress = totalSaldo
                            }
                        }
                    }
                } else {
                    holder.saldoTerkumpul.text = "Rp0,00"
                    holder.tvProgress.setText(R.string.capaian_belum)
                    holder.tvProgress.setTextColor(Color.RED)
                    println("Data tidak ditemukan di TotalSaldo.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Gagal mendapatkan data dari TotalSaldo: ${error.message}")
            }
        })

        holder.menu.setOnClickListener {
            optionsMenuClickListener.onOptionsMenuClicked(position)
        }
    }

    override fun getItemCount(): Int {
        return targetList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val judul: TextView = itemView.findViewById(R.id.tv_judul)
        val tanggalMulai: TextView = itemView.findViewById(R.id.tv_tanggalMulai)
        val tanggalAkhir: TextView = itemView.findViewById(R.id.tv_tanggalAkhir)
        val saldoTerkumpul: TextView = itemView.findViewById(R.id.tv_saldoTerkumpul)
        val saldoTarget: TextView = itemView.findViewById(R.id.tv_saldoTarget)
        val menu: ImageButton = itemView.findViewById(R.id.menuBtn)
        val tvProgress: TextView = itemView.findViewById(R.id.tv_progres)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<Target>) {
        targetList.clear()
        targetList.addAll(data)
        notifyDataSetChanged()
    }

    interface OptionsMenuClickListener {
        fun onOptionsMenuClicked(position: Int)
    }

    @SuppressLint("SimpleDateFormat")
    private fun timestampToDate(timestamp: Long): String {
        val date = Date(timestamp * 1000L)
        val sdf = SimpleDateFormat("dd MMMM yyyy")
        return sdf.format(date)
    }
}