package com.d3if2099.jagomenabung.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
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

class AdapterUserTarget(private val targetList: ArrayList<Target>,private val listener: onItemClicklistener): RecyclerView.Adapter<AdapterUserTarget.MyViewHolder>() {
    private val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_target, parent, false)
        return MyViewHolder(itemView)
    }

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item: Target = targetList[position]
        holder.judul.text = item.judul
        val id = item.id.toString()
        val mulai = item.tanggalMulai!! / 1000
        val akhir = item.tanggalBerakhir!! / 1000
        val tanggalMulai = timestampToDate(mulai)
        val tanggalBerakhir = timestampToDate(akhir)
        holder.tanggalMulai.text = tanggalMulai
        holder.tanggalAkhir.text = tanggalBerakhir
        val saldo = item.jumlahSaldo
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val saldoformatterTarget = formatter.format(saldo.toString().toDouble()).replace(",00", "")
        holder.saldoTarget.text = saldoformatterTarget

        val saldoTerkumpul = item.saldoTerkumpul
        val saldoformatterTerkumpul = formatter.format(saldoTerkumpul.toString().toDouble()).replace(",00", "")

        val capaianRef = FirebaseDatabase.getInstance().reference.child("Capaian").child(firebaseUser.uid).child(id)

        capaianRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val aktif = snapshot.child("aktif").getValue(Boolean::class.java) ?: false
                val kunci = snapshot.child("kunci").getValue(Boolean::class.java) ?: false
                if (aktif){
                    if(saldoTerkumpul!! == saldo!!){
                        if (kunci){
                            holder.tvProgress.setText(R.string.capaian)
                            holder.tvProgress.setTextColor(Color.parseColor("#03A309"))
                            holder.saldoTerkumpul.text = saldoformatterTerkumpul
                            holder.tvProgress.visibility = View.VISIBLE
                            holder.imgKunci.visibility = View.VISIBLE
                            holder.tvKet.visibility = View.GONE
                        } else {
                            holder.tvProgress.setText(R.string.capaian)
                            holder.tvProgress.setTextColor(Color.parseColor("#03A309"))
                            holder.saldoTerkumpul.text = saldoformatterTerkumpul
                            holder.tvProgress.visibility = View.VISIBLE
                            holder.imgKunci.visibility = View.GONE
                            holder.tvKet.visibility = View.GONE
                        }
                    } else {
                        holder.saldoTerkumpul.text = saldoformatterTerkumpul
                        holder.tvProgress.visibility = View.GONE
                        holder.tvKet.visibility = View.GONE
                    }
                }else {
                    holder.tvProgress.text = "Tidak Tercapai"
                    holder.tvProgress.setTextColor(Color.RED)
                    holder.saldoTerkumpul.text = saldoformatterTerkumpul
                    holder.tvProgress.visibility = View.VISIBLE
                    holder.tvKet.visibility = View.VISIBLE
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        holder.progressBar.max = saldo!!
        holder.progressBar.progress = saldoTerkumpul!!

        val persentaseProgres = (saldoTerkumpul.toDouble() / saldo) * 100
        holder.tvPersen.text = "Capaian : %.0f%%".format(persentaseProgres)

        holder.show.setOnClickListener { listener.onItemClick(item, it)}

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
        val tvProgress: TextView = itemView.findViewById(R.id.tv_progres)
        val tvKet: LinearLayout = itemView.findViewById(R.id.tvKeteranganGagal)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val tvPersen: TextView = itemView.findViewById(R.id.capaian_persen)
        val show : CardView = itemView.findViewById(R.id.listTarget)
        val imgKunci : ImageView = itemView.findViewById(R.id.btnImgKunci)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<Target>) {
        targetList.clear()
        targetList.addAll(data)
        notifyDataSetChanged()
    }

    interface onItemClicklistener {
        fun onItemClick(target : Target, v : View)
    }

    @SuppressLint("SimpleDateFormat")
    private fun timestampToDate(timestamp: Long): String {
        val date = Date(timestamp * 1000L)
        val sdf = SimpleDateFormat("dd MMMM yyyy")
        return sdf.format(date)
    }
}