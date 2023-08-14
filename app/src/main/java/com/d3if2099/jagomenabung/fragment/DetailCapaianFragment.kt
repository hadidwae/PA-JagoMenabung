package com.d3if2099.jagomenabung.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.d3if2099.jagomenabung.R
import com.d3if2099.jagomenabung.adapter.AdapterRiwayatSaldoCapaian
import com.d3if2099.jagomenabung.databinding.FragmentDetailCapaianBinding
import com.d3if2099.jagomenabung.model.KunciCapaian
import com.d3if2099.jagomenabung.model.RiwayatInputSaldo
import com.d3if2099.jagomenabung.model.Target
import com.d3if2099.jagomenabung.model.TotalSaldo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class DetailCapaianFragment : Fragment() {
    private lateinit var binding: FragmentDetailCapaianBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterRiwayatSaldoCapaian
    private lateinit var reference: DatabaseReference
    private val riwayatArrayList = arrayListOf<RiwayatInputSaldo>()
    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailCapaianBinding.inflate(layoutInflater, container, false)
        setHasOptionsMenu(true)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        reference = FirebaseDatabase.getInstance().reference.child("RiwayatInput").child(firebaseUser.uid)

        recyclerView = binding.rvInputSaldo
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = AdapterRiwayatSaldoCapaian(arrayListOf()){ riwayat ->
            deleteRiwayat(riwayat)
        }
        recyclerView.adapter = adapter

        setFragmentResultListener("id") { _, bundle ->
            val id = bundle.getString("id")
            detailCapaian(id)
            showRiwayat(id)
            updateTotalSaldo(id)
            binding.idCapaian.text = id
        }


        binding.btnInput.setOnClickListener {
            val target = binding.targetTIL.text.toString()
            val terkumpul = binding.tekumpulTIL.text.toString()
            val et = target.replace(".", "")
            val ul = terkumpul.replace(".", "")
            val getTarget = et.toInt()
            val getTerkumpul = ul.toInt()
            val total = getTarget - getTerkumpul
            val id = binding.idCapaian.text.toString()
            val ref = FirebaseDatabase.getInstance().reference.child("Capaian").child(firebaseUser.uid).child(id).child("kunci")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val kunci = snapshot.getValue(Boolean::class.java) ?: false
                        if (!kunci){
                            if (total != 0){
                                showTambahTargetDialog()
                            } else {
                                Toast.makeText(context, "Tidak Bisa Input Saldo Karena Sudah Mencapai Target", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        }

        binding.targetTIL.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int,
                count: Int
            ) {
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString() != current) {
                    binding.targetTIL.removeTextChangedListener(this)
                    val local = Locale("id", "id")
                    val replaceable = String.format(
                        "[Rp,.\\s]",
                        NumberFormat.getCurrencyInstance().currency!!
                            .getSymbol(local)
                    )
                    val cleanString = s.toString().replace(
                        replaceable.toRegex(),
                        ""
                    )
                    val parsed: Double = try {
                        cleanString.toDouble()
                    } catch (e: NumberFormatException) {
                        0.00
                    }
                    val formatter = NumberFormat
                        .getCurrencyInstance(local)
                    formatter.maximumFractionDigits = 0
                    formatter.isParseIntegerOnly = true
                    val formatted = formatter.format(parsed)
                    val replace = String.format(
                        "[Rp\\s]",
                        NumberFormat.getCurrencyInstance().currency!!
                            .getSymbol(local)
                    )
                    val clean = formatted.replace(replace.toRegex(), "")
                    current = formatted
                    binding.targetTIL.setText(clean)
                    binding.targetTIL.setSelection(clean.length)
                    binding.targetTIL.addTextChangedListener(this)
                }
            }
        })

        binding.tekumpulTIL.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int,
                count: Int
            ) {
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString() != current) {
                    binding.tekumpulTIL.removeTextChangedListener(this)
                    val local = Locale("id", "id")
                    val replaceable = String.format(
                        "[Rp,.\\s]",
                        NumberFormat.getCurrencyInstance().currency!!
                            .getSymbol(local)
                    )
                    val cleanString = s.toString().replace(
                        replaceable.toRegex(),
                        ""
                    )
                    val parsed: Double = try {
                        cleanString.toDouble()
                    } catch (e: NumberFormatException) {
                        0.00
                    }
                    val formatter = NumberFormat
                        .getCurrencyInstance(local)
                    formatter.maximumFractionDigits = 0
                    formatter.isParseIntegerOnly = true
                    val formatted = formatter.format(parsed)
                    val replace = String.format(
                        "[Rp\\s]",
                        NumberFormat.getCurrencyInstance().currency!!
                            .getSymbol(local)
                    )
                    val clean = formatted.replace(replace.toRegex(), "")
                    current = formatted
                    binding.tekumpulTIL.setText(clean)
                    binding.tekumpulTIL.setSelection(clean.length)
                    binding.tekumpulTIL.addTextChangedListener(this)
                }
            }
        })

        binding.tanggalAkhirTIL.setOnClickListener {
            if (isEditMode){
                val tanggalMulai = binding.tanggalMualiTIL.text.toString()
                showDatePicker(tanggalMulai)
            }
        }

        binding.btnSimpan.setOnClickListener {
            val judul = binding.judulTIL.text.toString()
            val tanggalS = binding.tanggalAkhirTIL.text.toString()
            val salwal = binding.targetTIL.text.toString()
            val targetStr = salwal.replace(".", "")
            val target = targetStr.toInt()
            if (judul.isEmpty() || tanggalS.isEmpty() || salwal.isEmpty()) {
                Toast.makeText(context, "Gagal, pastikan data terisi dengan benar", Toast.LENGTH_SHORT).show()
            } else {
                val tanggal = convertStringToTimestamp(tanggalS)
                editCapaian(judul, tanggal, target)
            }
        }

        binding.btnKunci.setOnClickListener {
            updateKunciCapaian()
        }

        binding.btnBatal.setOnClickListener {
            isEditMode = false

            binding.judulTIL.isFocusable = false
            binding.judulTIL.isFocusableInTouchMode = false
            binding.judulTIL.isClickable = false
            binding.judulTIL.requestFocus()

            binding.targetTIL.isFocusable = false
            binding.targetTIL.isFocusableInTouchMode = false
            binding.targetTIL.isClickable = false
            binding.targetTIL.requestFocus()

            binding.saveCancel.visibility = View.GONE
            binding.linearInput.visibility = View.VISIBLE
        }

        return binding.root
    }

    private fun detailCapaian(id: String?) {
        val ref = FirebaseDatabase.getInstance().reference.child("Capaian").child(firebaseUser.uid).child(id.toString())
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val data = snapshot.getValue(Target::class.java)
                    val judul = data!!.judul.toString()
                    val taMu = data.tanggalMulai!!.toLong()
                    val taAk = data.tanggalBerakhir!!.toLong()
                    val target = data.jumlahSaldo.toString()
                    val terkumpul = data.saldoTerkumpul.toString()
                    val kunci = data.kunci

                    val taMulai = taMu / 1000
                    val taAkhir = taAk / 1000
                    val tanggalMulai = timestampToDate(taMulai)
                    val tanggalAkhir = timestampToDate(taAkhir)

                    if (terkumpul == target && kunci == false){
                        binding.btnKunci.visibility = View.VISIBLE
                    } else {
                        binding.btnKunci.visibility = View.GONE
                    }

                    if(terkumpul == target){
                        binding.btnInput.visibility = View.GONE
                    } else {
                        binding.btnInput.visibility = View.VISIBLE
                    }

                    binding.judulTIL.setText(judul)
                    binding.tanggalMualiTIL.setText(tanggalMulai)
                    binding.tanggalAkhirTIL.setText(tanggalAkhir)
                    binding.targetTIL.setText(target)
                    binding.tekumpulTIL.setText(terkumpul)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showRiwayat(id: String?) {
        val ref = FirebaseDatabase.getInstance().reference.child("RiwayatInput").child(firebaseUser.uid).child(id.toString())
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                riwayatArrayList.clear()
                if (snapshot.exists()) {
                    for (tranSnap in snapshot.children) {
                        val data = tranSnap.getValue(RiwayatInputSaldo::class.java)
                        riwayatArrayList.add(data!!)
                    }
                    adapter.setData(riwayatArrayList)
                    binding.tvRiwayat.visibility = View.GONE
                } else {
                    adapter.setData(riwayatArrayList)
                    binding.tvRiwayat.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun updateTotalSaldo(id: String?) {
        val ref = FirebaseDatabase.getInstance().reference.child("RiwayatInput").child(firebaseUser.uid).child(id.toString())
        val capaianRef = FirebaseDatabase.getInstance().reference.child("Capaian").child(firebaseUser.uid).child(id.toString())
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalSaldo = 0
                if (snapshot.exists()) {
                    for (tranSnap in snapshot.children) {
                        val data = tranSnap.getValue(RiwayatInputSaldo::class.java)
                        if (data != null){
                            totalSaldo += data.saldo ?: 0
                        }
                    }
                }
                val totalSaldoTerkumpulRef = capaianRef.child("saldoTerkumpul")
                totalSaldoTerkumpulRef.setValue(totalSaldo)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun editCapaian(judul: String, tanggal: Long, target: Int) {
        val idCapaian = binding.idCapaian.text.toString()
        val capaianRef = FirebaseDatabase.getInstance().reference.child("Capaian").child(firebaseUser.uid).child(idCapaian)
        capaianRef.updateChildren(
            mapOf(
                "judul" to judul,
                "tanggalBerakhir" to tanggal,
                "jumlahSaldo" to target
            )
        ).addOnSuccessListener {
            binding.judulTIL.isFocusable = false
            binding.judulTIL.isFocusableInTouchMode = false
            binding.judulTIL.isClickable = false
            binding.judulTIL.requestFocus()

            binding.targetTIL.isFocusable = false
            binding.targetTIL.isFocusableInTouchMode = false
            binding.targetTIL.isClickable = false
            binding.targetTIL.requestFocus()

            binding.saveCancel.visibility = View.GONE
            binding.linearInput.visibility = View.VISIBLE
            Toast.makeText(context, "Data Capaian Berhasil Diperbarui", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            binding.judulTIL.isFocusable = false
            binding.judulTIL.isFocusableInTouchMode = false
            binding.judulTIL.isClickable = false
            binding.judulTIL.requestFocus()

            binding.targetTIL.isFocusable = false
            binding.targetTIL.isFocusableInTouchMode = false
            binding.targetTIL.isClickable = false
            binding.targetTIL.requestFocus()

            binding.saveCancel.visibility = View.GONE
            binding.linearInput.visibility = View.VISIBLE
            Toast.makeText(context, "Gagal memperbarui data Capaian: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePicker(tanggalMulai: String) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val startDate = dateFormat.parse(tanggalMulai)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, monthOfYear, dayOfMonth)
                val selectedDate = selectedCalendar.time

                if (!selectedDate.before(startDate)) {
                    val formattedDate = dateFormat.format(selectedDate)
                    binding.tanggalAkhirTIL.setText(formattedDate)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = startDate!!.time

        datePickerDialog.show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun showTambahTargetDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_input_saldo, null)
        dialogBuilder.setView(dialogView)

        val saldo = dialogView.findViewById<EditText>(R.id.dialogSaldoTIL)

        saldo.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int,
                count: Int
            ) {
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString() != current) {
                    saldo.removeTextChangedListener(this)
                    val local = Locale("id", "id")
                    val replaceable = String.format(
                        "[Rp,.\\s]",
                        NumberFormat.getCurrencyInstance().currency!!
                            .getSymbol(local)
                    )
                    val cleanString = s.toString().replace(
                        replaceable.toRegex(),
                        ""
                    )
                    val parsed: Double = try {
                        cleanString.toDouble()
                    } catch (e: NumberFormatException) {
                        0.00
                    }
                    val formatter = NumberFormat
                        .getCurrencyInstance(local)
                    formatter.maximumFractionDigits = 0
                    formatter.isParseIntegerOnly = true
                    val formatted = formatter.format(parsed)
                    val replace = String.format(
                        "[Rp\\s]",
                        NumberFormat.getCurrencyInstance().currency!!
                            .getSymbol(local)
                    )
                    val clean = formatted.replace(replace.toRegex(), "")
                    current = formatted
                    saldo.setText(clean)
                    saldo.setSelection(clean.length)
                    saldo.addTextChangedListener(this)
                }
            }
        })

        dialogBuilder.setTitle("Tambah Saldo")
        dialogBuilder.setPositiveButton("Tambah") { dialog, _ ->
            val calendar = Calendar.getInstance()
            val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy")
            val tanggal: String = simpleDateFormat.format(calendar.time)
            val salwal = saldo.text.toString()
            val jumlahSaldoStr = salwal.replace(".", "")
            val getSaldo = jumlahSaldoStr.toInt()
            val target = binding.targetTIL.text.toString()
            val terkumpul = binding.tekumpulTIL.text.toString()
            val et = target.replace(".", "")
            val ul = terkumpul.replace(".", "")
            val getTarget = et.toInt()
            val getTerkumpul = ul.toInt()
            val total = getTarget - getTerkumpul
            val ref = FirebaseDatabase.getInstance().reference.child("TotalSaldo").child(firebaseUser.uid).child("saldo")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val saldoUser = snapshot.getValue(Int::class.java)
                        if (saldoUser!! >= getSaldo){
                            if(getSaldo <= total){
                                simpanRiwayat(tanggal, getSaldo)
                                dialog.dismiss()
                            } else {
                                simpanRiwayat(tanggal, total)
                                dialog.dismiss()
                            }
                        }
                        else {
                            Toast.makeText(context, "Saldo Utama Anda Tidak Cukup", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
        dialogBuilder.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun simpanRiwayat(tanggal: String, saldo: Int) {
        val id = binding.idCapaian.text.toString()
        val idSaldo = reference.push().key!!
        val riwayat = RiwayatInputSaldo(idSaldo, id, tanggal, saldo)
        reference.child(id).child(idSaldo).setValue(riwayat).addOnCompleteListener {
            if (it.isSuccessful){
                reference.child(id).child(idSaldo).setValue(riwayat)
                Toast.makeText(context, "Tambah Saldo Berhasil", Toast.LENGTH_SHORT).show()
                showRiwayat(id)
                updateTotalSaldo(id)
            }else {
                Toast.makeText(context, "Tambah Saldo Gagal", Toast.LENGTH_SHORT).show()
                showRiwayat(id)
                updateTotalSaldo(id)
            }
        }
    }

    private fun updateKunciCapaian() {
        val id = binding.idCapaian.text.toString()
        val terkumpul = binding.tekumpulTIL.text.toString()
        val ter = terkumpul.replace(".", "")
        val saldo = ter.toInt()
        val salkun = KunciCapaian(id, saldo)
        val capaianRef = FirebaseDatabase.getInstance().reference.child("Capaian").child(firebaseUser.uid).child(id)
        AlertDialog.Builder(context).apply {
            setTitle("Kunci Capaian?")
            setMessage("Ket : Jika pencapaian dikunci, saldo anda tidak akan kembali meskipun pencapaian dihapus")
            setPositiveButton("Ya") { _, _ ->
                capaianRef.child("kunci").setValue(true)
                    .addOnSuccessListener {
                        val ref = FirebaseDatabase.getInstance().reference.child("KunciCapaian").child(firebaseUser.uid).child(id)
                        ref.setValue(salkun)
                        Toast.makeText(context, "Berhasil Dikunci", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(context, "Gagal", Toast.LENGTH_SHORT).show()
                    }
            }
            setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            show()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun timestampToDate(timestamp: Long): String {
        val date = Date(timestamp * 1000L)
        val sdf = SimpleDateFormat("dd MMMM yyyy")
        return sdf.format(date)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_target, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_edit -> {
                val id = binding.idCapaian.text.toString()
                val ref = FirebaseDatabase.getInstance().reference.child("Capaian").child(firebaseUser.uid).child(id).child("kunci")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val kunci = snapshot.getValue(Boolean::class.java) ?: false
                            if (!kunci){
                                isEditMode = true

                                binding.targetTIL.isFocusable = true
                                binding.targetTIL.isFocusableInTouchMode = true
                                binding.targetTIL.isClickable = true
                                binding.targetTIL.requestFocus()

                                binding.judulTIL.isFocusable = true
                                binding.judulTIL.isFocusableInTouchMode = true
                                binding.judulTIL.isClickable = true
                                binding.judulTIL.requestFocus()

                                binding.saveCancel.visibility = View.VISIBLE
                                binding.linearInput.visibility = View.GONE
                            }
                            else {
                                Toast.makeText(context, "Capaian Tidak Bisa Diedit Karena Sudah Terkunci", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

                true
            }

            R.id.menu_delete -> {
                hapusCapaian()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun hapusCapaian() {
        AlertDialog.Builder(context).apply {
            setMessage("Hapus Capaian?")
            setPositiveButton("HAPUS") { _, _ ->
                val idCapaian = binding.idCapaian.text.toString()
                val ref = FirebaseDatabase.getInstance().reference.child("Capaian").child(firebaseUser.uid).child(idCapaian)
                val task = ref.removeValue()
                task.addOnSuccessListener{
                    Toast.makeText(context,"Capaian Berhasil Dihapus", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }.addOnFailureListener{
                    Toast.makeText(context,"Capaian Gagal Dihapus", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            show()
        }
    }

    private fun deleteRiwayat(riwayat: RiwayatInputSaldo) {
        AlertDialog.Builder(context).apply {
            setMessage("Hapus riwayat?")
            setPositiveButton("Ya") { _, _ ->
                val id = riwayat.idCapaian
                val ref = FirebaseDatabase.getInstance().reference.child("RiwayatInput").child(firebaseUser.uid).child(id.toString()).child(riwayat.id.toString())
                val task = ref.removeValue()
                task.addOnSuccessListener{
                    Toast.makeText(context,"Riwayat Berhasil Dihapus", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener{
                    Toast.makeText(context,"Riwayat Gagal Dihapus", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            show()
        }
    }

    private fun convertStringToTimestamp(dateString: String): Long {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        return date?.time ?: 0
    }
}