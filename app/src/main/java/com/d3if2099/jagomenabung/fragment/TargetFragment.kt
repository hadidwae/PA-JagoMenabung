package com.d3if2099.jagomenabung.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.d3if2099.jagomenabung.R
import com.d3if2099.jagomenabung.adapter.AdapterUserTarget
import com.d3if2099.jagomenabung.databinding.FragmentTargetBinding
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TargetFragment : Fragment() {

    private lateinit var binding: FragmentTargetBinding
    private lateinit var ref: DatabaseReference
    private lateinit var saldoRef: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterUserTarget
    private lateinit var firebaseUser : FirebaseUser
    private val targetArrayList = arrayListOf<Target>()
    private var selectedDateMulai: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTargetBinding.inflate(layoutInflater, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        saldoRef = FirebaseDatabase.getInstance().reference.child("TotalSaldo")

        recyclerView = binding.recyclerViewTarget
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        adapter = AdapterUserTarget(arrayListOf() , object : AdapterUserTarget.OptionsMenuClickListener {
            override fun onOptionsMenuClicked(position: Int) {
                performOptionsMenuClick(position)
            }
        })
        recyclerView.adapter = adapter

        binding.btnTambahTarget.setOnClickListener {
            showTambahTargetDialog()
        }

        binding.fab.setOnClickListener {
            showTambahTargetDialog()
        }

        showList()

        return binding.root
    }

    private fun showList() {
        ref = FirebaseDatabase.getInstance().reference.child("Target").child(firebaseUser.uid)
        ref.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                targetArrayList.clear()
                if(snapshot.exists()){
                    for(tranSnap in snapshot.children) {
                        val target = tranSnap.getValue(Target::class.java)
                        targetArrayList.add(target!!)
                    }
                    adapter.setData(targetArrayList)
                    binding.kosong.visibility = View.GONE
                    binding.fab.visibility = View.VISIBLE
                }else {
                    adapter.setData(targetArrayList)
                    binding.kosong.visibility = View.VISIBLE
                    binding.fab.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun performOptionsMenuClick(position: Int) {
        val popupMenu = PopupMenu(context , binding.recyclerViewTarget[position].findViewById(R.id.menuBtn))
        popupMenu.inflate(R.menu.menu_target)
        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when(item?.itemId){
                    R.id.menu_delete -> {
                        val target = targetArrayList[position]
                        val id = target.id!!
                        ref = FirebaseDatabase.getInstance().reference.child("Target").child(firebaseUser.uid).child(id)
                        ref.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    snapshot.ref.removeValue()
                                    Toast.makeText(
                                        context,
                                        "Berhasil menghapus target",
                                        Toast.LENGTH_SHORT).show()
                                    adapter.setData(targetArrayList)
                                } else {
                                    adapter.setData(targetArrayList)
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })

                        return true
                    }
                    // in the same way you can implement others
                    R.id.menu_edit -> {
                        // define
                        Toast.makeText(context , "Edit diklik" , Toast.LENGTH_SHORT).show()
                        return true
                    }
                }
                return false
            }
        })
        popupMenu.show()
    }

    private fun showTambahTargetDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_tambah_target, null)
        dialogBuilder.setView(dialogView)

        val judul = dialogView.findViewById<EditText>(R.id.dialogJudulTIL)
        val tanggalMulai = dialogView.findViewById<EditText>(R.id.dialogTanggalMulaiTIL)
        val tanggalAkhir = dialogView.findViewById<EditText>(R.id.dialogTanggalAkhirTIL)
        val saldo = dialogView.findViewById<EditText>(R.id.dialogJumlahTIL)

        val calendar = Calendar.getInstance()
        val tanggalMulaiCalendar = calendar.timeInMillis

        judul.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.isNotEmpty()) {
                    val capitalizedText = charSequence.toString().substring(0, 1).toUpperCase() +
                            charSequence.toString().substring(1)
                    judul.removeTextChangedListener(this)
                    judul.setText(capitalizedText)
                    judul.setSelection(capitalizedText.length)
                    judul.addTextChangedListener(this)
                }
            }
            override fun afterTextChanged(editable: Editable) {}
        })

        tanggalMulai.setOnClickListener {
            showDatePicker(tanggalMulai)
        }

        tanggalAkhir.setOnClickListener {
            showDatePicker(tanggalAkhir, tanggalMulaiCalendar)
        }

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

        dialogBuilder.setTitle("Tambah Data")
        dialogBuilder.setPositiveButton("Tambah") { dialog, _ ->
            val getId = ref.push().key!!
            val getJudul = judul.text.toString()
            val mulai = tanggalMulai.text.toString()
            val akhir = tanggalAkhir.text.toString()
            val getTanggalMulai = convertStringToTimestamp(mulai)
            val getTanggalAkhir = convertStringToTimestamp(akhir)
            val salwal = saldo.text.toString()
            val jumlahSaldoStr = salwal.replace(".", "")
            val getSaldo = jumlahSaldoStr.toInt()



            simpanTarget(getId, getJudul, getTanggalMulai, getTanggalAkhir, getSaldo)
            dialog.dismiss()
        }
        dialogBuilder.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun simpanTarget(getId: String, getJudul : String, getTanggalMulai: Long, getTanggalAkhir: Long, getSaldo: Int) {
        val target = Target(getId, getJudul, getTanggalMulai, getTanggalAkhir, getSaldo)
        ref = FirebaseDatabase.getInstance().reference.child("Target")
        ref.child(firebaseUser.uid).child(getId).setValue(target).addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(context, R.string.tambah_target, Toast.LENGTH_SHORT).show()
                showList()
            }else{
                showList()
                val message = it.exception!!.toString()
                Toast.makeText(context,"Error : $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker(editText: EditText, minDate: Long? = null) {
        val calendar = Calendar.getInstance()

        val selectedDate = getSelectedDateFromString(editText.text.toString())

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, monthOfYear, dayOfMonth)
                val selectedDate = selectedCalendar.time

                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate)

                editText.setText(formattedDate)

                if (editText.id == R.id.dialogTanggalMulaiTIL) {
                    selectedDateMulai = selectedDate
                }
            },
            // Set the selectedDateMulai as the default date for the first date picker
            selectedDateMulai?.let { it.year + 1900 } ?: calendar.get(Calendar.YEAR),
            selectedDateMulai?.let { it.month } ?: calendar.get(Calendar.MONTH),
            selectedDateMulai?.let { it.date } ?: calendar.get(Calendar.DAY_OF_MONTH)
        )

        if (minDate != null && editText.id == R.id.dialogTanggalAkhirTIL) {
            datePickerDialog.datePicker.minDate = minDate
        }

        datePickerDialog.show()
    }

    private fun getSelectedDateFromString(dateString: String): Date? {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return try {
            dateFormat.parse(dateString)
        } catch (e: ParseException) {
            null
        }
    }

    private fun convertStringToTimestamp(dateString: String): Long {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        return date?.time ?: 0
    }

}