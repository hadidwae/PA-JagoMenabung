package com.d3if2099.jagomenabung.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.d3if2099.jagomenabung.R
import com.d3if2099.jagomenabung.antarmuka.DateSelected
import com.d3if2099.jagomenabung.databinding.FragmentShowTransaksiBinding
import com.d3if2099.jagomenabung.model.Transaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ShowTransaksiFragment : Fragment(), DateSelected {
    private lateinit var binding : FragmentShowTransaksiBinding
    private lateinit var firebaseUser : FirebaseUser
    private lateinit var ref: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShowTransaksiBinding.inflate(layoutInflater, container, false)
        auth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        ref = FirebaseDatabase.getInstance().reference.child("Transaksi")

        setTransaksi()

        binding.tanggalTIL.setOnClickListener {
            showDatePicker()
        }

        binding.btnhapus.setOnClickListener {
            hapusTransaksi()
        }

        binding.jumlahTIL.addTextChangedListener(object : TextWatcher {
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
                    binding.jumlahTIL.removeTextChangedListener(this)
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
                    binding.jumlahTIL.setText(clean)
                    binding.jumlahTIL.setSelection(clean.length)
                    binding.jumlahTIL.addTextChangedListener(this)
                }
            }
        })

        binding.btnSimpan.setOnClickListener {
            setFragmentResultListener("id") { _, bundle ->
                val result = bundle.getString("id")
                val kategori = binding.kategoriSpinner.text.toString()
                val tanggalS = binding.tanggalTIL.text.toString()
                val judul = binding.judulTIL.text.toString()
                val salwal = binding.jumlahTIL.text.toString()
                val jumlahSaldoStr = salwal.replace(".", "")
                val jumlahSaldo = jumlahSaldoStr.toInt()
                val keterangan = binding.keteranganTIL.text.toString()
                if (kategori.isEmpty() && tanggalS.isEmpty() && judul.isEmpty() && salwal.isEmpty()) {
                    Toast.makeText(context, "Gagal, pastikan data terisi dengan benar", Toast.LENGTH_SHORT).show()
                } else {
                    val tanggal = convertStringToTimestamp(tanggalS)
                    editTransaksi(result, kategori, tanggal, judul, jumlahSaldo, keterangan)
                }
            }
        }
        return binding.root
    }

    private fun setTransaksi() {
        setFragmentResultListener("kategori") { _, bundle ->
            val result = bundle.getString("kategori")
            binding.kategoriSpinner.setText(result)
        }
        setFragmentResultListener("tanggal") { _, bundle ->
            val result = bundle.getString("tanggal").toString()
            val tm = result.toLong()
            val tanggalS = tm / 1000
            val tanggal = timestampToDate(tanggalS)
            binding.tanggalTIL.setText(tanggal)
        }
        setFragmentResultListener("judul") { _, bundle ->
            val result = bundle.getString("judul")
            binding.judulTIL.setText(result)
        }
        setFragmentResultListener("jumlahSaldo") { _, bundle ->
            val result = bundle.getInt("jumlahSaldo").toString()
            binding.jumlahTIL.setText(result)
        }
        setFragmentResultListener("keterangan") { _, bundle ->
            val result = bundle.getString("keterangan")
            binding.keteranganTIL.setText(result)
        }
    }

    private fun showDatePicker() {
        val datePickerFragment = DatePickerFragment(this)
        datePickerFragment.show(requireFragmentManager(), "datePicker")
    }

    class DatePickerFragment(private val dateSelected: ShowTransaksiFragment) : DialogFragment(), DatePickerDialog.OnDateSetListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            return  DatePickerDialog(requireContext(), this, year, month, day )
        }

        override fun onDateSet(view: DatePicker?, year: Int, month : Int, day : Int) {
            dateSelected.receiveDate(year, month, day)
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun receiveDate(year: Int, month: Int, day: Int) {
        val calendar = GregorianCalendar()
        calendar.set(Calendar.DAY_OF_MONTH,day)
        calendar.set(Calendar.MONTH,month)
        calendar.set(Calendar.YEAR,year)

        val viewFormatter = SimpleDateFormat("dd MMMM yyyy")
        val viewFormattedDate : String = viewFormatter.format(calendar.time)

        binding.tanggalTIL.setText(viewFormattedDate)
    }

    private fun editTransaksi(result: String?, kategori: String, tanggal: Long, judul: String, jumlahSaldo: Int, keterangan: String) {
        val currentUserId = auth.currentUser!!.uid
        val idTransaksi = ref.push().key!!
        val trans = Transaksi(idTransaksi, kategori, tanggal, judul, jumlahSaldo, keterangan)

        ref.child(currentUserId).child(idTransaksi).setValue(trans).addOnCompleteListener {
            if (it.isSuccessful){
                FirebaseDatabase.getInstance().reference.child("Transaksi").child(currentUserId).child(result!!).removeValue()
                Toast.makeText(context, "Edit transaksi berhasil", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }else{
                val message = it.exception!!.toString()
                Toast.makeText(context,"Error : $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hapusTransaksi() {
        AlertDialog.Builder(context).apply {
            setMessage(R.string.pesan_hapus_transaksi)
            setPositiveButton("HAPUS") { _, _ ->
                setFragmentResultListener("id") { _, bundle ->
                    val idTransaksi = bundle.getString("id").toString()
                    val tanggal = binding.tanggalItem.text.toString()
                    val currentUserId = auth.currentUser!!.uid
                    ref = FirebaseDatabase.getInstance().reference.child("Transaksi").child(currentUserId).child(idTransaksi)
                    val task = ref.removeValue()
                    task.addOnSuccessListener{
                        Toast.makeText(context,"Transaksi Berhasil Dihapus", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }.addOnFailureListener{ tast ->
                        Toast.makeText(context,"Transaksi Menghapus Karyawan${tast.message}", Toast.LENGTH_SHORT).show()
                    }
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

    @SuppressLint("SimpleDateFormat")
    private fun timestampToDate(timestamp: Long): String {
        val date = Date(timestamp * 1000L)
        val sdf = SimpleDateFormat("dd MMMM yyyy")
        return sdf.format(date)
    }

    override fun onResume() {
        super.onResume()
        val kategori = resources.getStringArray(R.array.kategori_transaksi)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_kategori, kategori)
        binding.kategoriSpinner.setAdapter(arrayAdapter)
    }
}