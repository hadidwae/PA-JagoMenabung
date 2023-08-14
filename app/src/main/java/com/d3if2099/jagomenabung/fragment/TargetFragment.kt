package com.d3if2099.jagomenabung.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.d3if2099.jagomenabung.R
import com.d3if2099.jagomenabung.adapter.AdapterUserTarget
import com.d3if2099.jagomenabung.databinding.FragmentTargetBinding
import com.d3if2099.jagomenabung.model.Target
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
    ): View {
        binding = FragmentTargetBinding.inflate(layoutInflater, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        saldoRef = FirebaseDatabase.getInstance().reference.child("TotalSaldo")

        recyclerView = binding.recyclerViewTarget
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        adapter = AdapterUserTarget(arrayListOf(), object : AdapterUserTarget.onItemClicklistener {
            override fun onItemClick(target: Target, v: View) {
                val id = target.id
                setFragmentResult(
                    "id",
                    bundleOf("id" to id)
                )
                findNavController().navigate(R.id.action_targetFragment_to_detailCapaianFragment)
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
        ref = FirebaseDatabase.getInstance().reference.child("Capaian").child(firebaseUser.uid)
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

    @SuppressLint("SimpleDateFormat")
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

        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy")
        val currentDateAndTime: String = simpleDateFormat.format(Date())
        tanggalMulai.setText(currentDateAndTime)

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

        dialogBuilder.setTitle("Tambah Capaian")
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
        val target = Target(getId, getJudul, getTanggalMulai, getTanggalAkhir, getSaldo, 0, true, kunci = false)
        ref = FirebaseDatabase.getInstance().reference.child("Capaian")
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
            selectedDateMulai?.month ?: calendar.get(Calendar.MONTH),
            selectedDateMulai?.date ?: calendar.get(Calendar.DAY_OF_MONTH)
        )

        if (minDate != null && editText.id == R.id.dialogTanggalAkhirTIL) {
            datePickerDialog.datePicker.minDate = minDate
        }

        datePickerDialog.show()
    }

    private fun convertStringToTimestamp(dateString: String): Long {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        return date?.time ?: 0
    }
}