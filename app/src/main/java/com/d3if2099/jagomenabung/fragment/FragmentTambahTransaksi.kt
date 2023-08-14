package com.d3if2099.jagomenabung.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.d3if2099.jagomenabung.databinding.FragmentTambahTransaksiBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.d3if2099.jagomenabung.R
import com.d3if2099.jagomenabung.antarmuka.DateSelected
import com.d3if2099.jagomenabung.model.Transaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentTambahTransaksi : Fragment(), DateSelected {
    private lateinit var binding: FragmentTambahTransaksiBinding
    private lateinit var firebaseUser : FirebaseUser
    private lateinit var ref: DatabaseReference
    private lateinit var ref2: DatabaseReference
    private lateinit var ref4: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTambahTransaksiBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        ref = FirebaseDatabase.getInstance().getReference("Transaksi")
        ref2 = FirebaseDatabase.getInstance().getReference("TotalSaldo")
        ref4 = FirebaseDatabase.getInstance().getReference("Saldo")

        currentDate()

        binding.tanggalTIL.setOnClickListener {
            showDatePicker()
        }

        binding.btnSimpan.setOnClickListener {
            val idTransaksi = ref.push().key!!
            val kategori = binding.kategoriSpinner.text.toString()
            val tanggalS = binding.tanggalTIL.text.toString()
            val judul = binding.judulTIL.text.toString()
            val salwal = binding.jumlahTIL.text.toString()
            val keterangan = binding.keteranganTIL.text.toString()


            if(judul.isEmpty() && salwal.isEmpty() && keterangan.isEmpty()){
                Toast.makeText(context, R.string.gagal_isi_data, Toast.LENGTH_SHORT).show()
            } else if(judul.isEmpty() && salwal.isNotEmpty() && keterangan.isEmpty()){
                Toast.makeText(context, R.string.gagal_isi_data, Toast.LENGTH_SHORT).show()
            } else if(judul.isEmpty() && salwal.isNotEmpty() && keterangan.isNotEmpty()){
                Toast.makeText(context, R.string.gagal_isi_data, Toast.LENGTH_SHORT).show()
            } else if(judul.isNotEmpty() && salwal.isNotEmpty() && keterangan.isEmpty()){
                Toast.makeText(context, R.string.gagal_isi_data, Toast.LENGTH_SHORT).show()
            } else if(judul.isEmpty() && salwal.isEmpty() && keterangan.isNotEmpty()){
                Toast.makeText(context, R.string.gagal_isi_data, Toast.LENGTH_SHORT).show()
            } else if(salwal.isNotEmpty()) {
                val jumlahSaldoStr = salwal.replace(".", "")
                val jumlahSaldo = jumlahSaldoStr.toInt()
                val tanggal = convertStringToTimestamp(tanggalS)
                tambahTransaksi(idTransaksi, kategori, tanggal, judul, jumlahSaldo, keterangan)
            }
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

        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    private fun currentDate(){
        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy")
        val currentDateAndTime: String = simpleDateFormat.format(Date())

        binding.tanggalTIL.setText(currentDateAndTime)
    }

    private fun showDatePicker() {
        val datePickerFragment = DatePickerFragment(this)
        datePickerFragment.show(requireFragmentManager(), "datePicker")
    }

    class DatePickerFragment(private val dateSelected: DateSelected) : DialogFragment(), DatePickerDialog.OnDateSetListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), this, year, month, day)

            val maxDate = System.currentTimeMillis()
            datePickerDialog.datePicker.maxDate = maxDate

            return datePickerDialog
        }

        override fun onDateSet(view: DatePicker?, year: Int, month : Int, day : Int) {
            dateSelected.receiveDate(year, month, day)
        }
    }

    private fun tambahTransaksi(
        idTransaksi: String,
        kategori: String,
        tanggal: Long,
        judul: String,
        jumlahSaldo: Int,
        keterangan: String,
    ) {
        val currentUserId = auth.currentUser!!.uid
        val trans = Transaksi(idTransaksi, kategori, tanggal, judul, jumlahSaldo, keterangan)
        ref.child(currentUserId).child(idTransaksi).setValue(trans).addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(context, R.string.tambah_berhasil, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()

            }else{
                val message = it.exception!!.toString()
                Toast.makeText(context,"Error : $message", Toast.LENGTH_SHORT).show()
            }
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

    private fun convertStringToTimestamp(dateString: String): Long {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        return date?.time ?: 0
    }

    override fun onResume() {
        super.onResume()
        val kategori = resources.getStringArray(R.array.kategori_transaksi)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_kategori, kategori)
        binding.kategoriSpinner.setAdapter(arrayAdapter)
    }

    override fun onStart() {
        super.onStart()
        currentDate()
    }
}