package com.d3if2099.jagomenabung.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.d3if2099.jagomenabung.R
import com.d3if2099.jagomenabung.activity.LoginActivity
import com.d3if2099.jagomenabung.adapter.AdapterGrupTransaksi
import com.d3if2099.jagomenabung.adapter.AdapterUserTransaksi
import com.d3if2099.jagomenabung.antarmuka.DateSelectedMonth
import com.d3if2099.jagomenabung.databinding.FragmentHomeBinding
import com.d3if2099.jagomenabung.model.KunciCapaian
import com.d3if2099.jagomenabung.model.Target
import com.d3if2099.jagomenabung.model.Transaksi
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.DateFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(), AdapterUserTransaksi.onItemClicklistener, DateSelectedMonth {
    private lateinit var binding : FragmentHomeBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var firebaseUser : FirebaseUser
    private lateinit var ref2: DatabaseReference
    private lateinit var ref3: DatabaseReference
    private val transaksiList = mutableListOf<Transaksi>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        auth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        ref2 = FirebaseDatabase.getInstance().getReference("TotalSaldo")
        ref3 = FirebaseDatabase.getInstance().getReference("Saldo")

        currentDate()
        cekCapaian()

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        binding.tvBulan.setOnClickListener {
            showMonthYearPicker()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_fragmentTambahTransaksi)
        }

        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    private fun currentDate(){
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("MMMM yyyy")
        var currentDateAndTime: String = simpleDateFormat.format(calendar.time)

        binding.tvBulan.text = currentDateAndTime
        showTransaksi { transaksi ->
            val groupedTransactions = groupTransactionsByDate(transaksi)
            val groupedTransactionAdapter = AdapterGrupTransaksi(groupedTransactions, this)
            recyclerView.adapter = groupedTransactionAdapter
        }
        updateTotalSaldo()

        binding.previous.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            currentDateAndTime = simpleDateFormat.format(calendar.time)
            binding.tvBulan.text = currentDateAndTime
            updateTotalSaldo()
            showTransaksi { transaksi ->
                val groupedTransactions = groupTransactionsByDate(transaksi)
                val groupedTransactionAdapter = AdapterGrupTransaksi(groupedTransactions, this)
                recyclerView.adapter = groupedTransactionAdapter
            }
        }

        binding.next.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            currentDateAndTime = simpleDateFormat.format(calendar.time)
            binding.tvBulan.text = currentDateAndTime
            updateTotalSaldo()
            showTransaksi { transaksi ->
                val groupedTransactions = groupTransactionsByDate(transaksi)
                val groupedTransactionAdapter = AdapterGrupTransaksi(groupedTransactions, this)
                recyclerView.adapter = groupedTransactionAdapter
            }
        }
    }

    private fun showMonthYearPicker() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_datepicker, null)

        val monthPicker = view.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = view.findViewById<NumberPicker>(R.id.yearPicker)
        val confirmButton = view.findViewById<Button>(R.id.confirmButton)

        val calendar = Calendar.getInstance()

        val month = calendar.get(Calendar.MONTH)
        val months = DateFormatSymbols().months
        monthPicker.minValue = 0
        monthPicker.maxValue = months.size - 1
        monthPicker.displayedValues = months
        monthPicker.value = month

        val year = calendar.get(Calendar.YEAR)
        yearPicker.minValue = year - 10
        yearPicker.maxValue = year + 10
        yearPicker.value = year

        val alertDialogBuilder = AlertDialog.Builder(context)
            .setView(view)

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

        confirmButton.setOnClickListener {
            val selectedMonth = monthPicker.value
            val selectedYear = yearPicker.value
            receiveDateMonth(selectedYear, selectedMonth)
            alertDialog.dismiss()
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun receiveDateMonth(year: Int, month: Int) {
        val calendar = GregorianCalendar()
        calendar.set(Calendar.MONTH,month)
        calendar.set(Calendar.YEAR,year)

        val viewFormatter = SimpleDateFormat("MMMM yyyy")
        var viewFormattedDate : String = viewFormatter.format(calendar.time)

        binding.tvBulan.text = viewFormattedDate

        binding.previous.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            viewFormattedDate = viewFormatter.format(calendar.time)
            binding.tvBulan.text = viewFormattedDate
            updateTotalSaldo()
            showTransaksi { transaksi ->
                val groupedTransactions = groupTransactionsByDate(transaksi)
                val groupedTransactionAdapter = AdapterGrupTransaksi(groupedTransactions, this)
                recyclerView.adapter = groupedTransactionAdapter
            }
        }

        binding.next.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            viewFormattedDate = viewFormatter.format(calendar.time)
            binding.tvBulan.text = viewFormattedDate
            updateTotalSaldo()
            showTransaksi { transaksi ->
                val groupedTransactions = groupTransactionsByDate(transaksi)
                val groupedTransactionAdapter = AdapterGrupTransaksi(groupedTransactions, this)
                recyclerView.adapter = groupedTransactionAdapter
            }
        }

        updateTotalSaldo()
        showTransaksi { transaksi ->
            val groupedTransactions = groupTransactionsByDate(transaksi)
            val groupedTransactionAdapter = AdapterGrupTransaksi(groupedTransactions, this)
            recyclerView.adapter = groupedTransactionAdapter
        }
    }

    private fun showTransaksi(completion: (List<Transaksi>) -> Unit) {
        val tvBulan = binding.tvBulan.text.toString()
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = dateFormat.parse(tvBulan)!!

        val bulan = cal.get(Calendar.MONTH)
        val tahun = cal.get(Calendar.YEAR)
        val transaksiRef = FirebaseDatabase.getInstance().reference.child("Transaksi").child(firebaseUser.uid)
        transaksiRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transaksiList.clear()
                for (transactionSnapshot in snapshot.children) {
                    val data = transactionSnapshot.getValue(Transaksi::class.java)
                    if (data != null) {
                        val transactionCal = Calendar.getInstance()
                        transactionCal.timeInMillis = data.tanggal!!
                        val transactionMonth = transactionCal.get(Calendar.MONTH)
                        val transactionYear = transactionCal.get(Calendar.YEAR)
                        if (transactionMonth == bulan && transactionYear == tahun) {
                            transaksiList.add(data)
                        }
                    }
                }
                transaksiList.sortByDescending { it.tanggal }
                completion(transaksiList)
            }
            override fun onCancelled(error: DatabaseError) {
                completion(emptyList())
            }
        })
    }

    private fun showListTransaksiFilter(getJudul: String, getTanggal: Long, getKategori: String, completion: (List<Transaksi>) -> Unit) {
        val tvBulan = binding.tvBulan.text.toString()
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = dateFormat.parse(tvBulan)!!

        val bulan = cal.get(Calendar.MONTH)
        val tahun = cal.get(Calendar.YEAR)

        val transaksiRef = FirebaseDatabase.getInstance().reference.child("Transaksi").child(firebaseUser.uid)
        transaksiRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transaksiList = mutableListOf<Transaksi>() // Create a new list for filtered transactions
                for (transactionSnapshot in snapshot.children) {
                    val data = transactionSnapshot.getValue(Transaksi::class.java)
                    data?.let {
                        val transactionCal = Calendar.getInstance()
                        transactionCal.timeInMillis = it.tanggal!!
                        val kategori = it.kategori.toString()
                        val judul = it.judul.toString()
                        val tanggal = it.tanggal.toLong()

                        val transactionMonth = transactionCal.get(Calendar.MONTH)
                        val transactionYear = transactionCal.get(Calendar.YEAR)

                        if ((kategori == getKategori || getKategori == "Semua") &&
                            transactionMonth == bulan && transactionYear == tahun) {
                            if (getJudul.isEmpty() || judul.contains(getJudul, ignoreCase = true)) {
                                if (getTanggal == 0L || tanggal == getTanggal) {
                                    transaksiList.add(it)
                                }
                            }
                        }
                    }
                }
                transaksiList.sortByDescending { it.tanggal }
                completion(transaksiList)
            }

            override fun onCancelled(error: DatabaseError) {
                completion(emptyList())
            }
        })
    }

    private fun groupTransactionsByDate(transactions: List<Transaksi>): Map<Long, List<Transaksi>> {
        return transactions.groupBy { it.tanggal!! }
    }

    private fun updateTotalSaldo() {
        val saldoRef = ref2.child(firebaseUser.uid)
        val transaksiRef = FirebaseDatabase.getInstance().reference.child("Transaksi").child(firebaseUser.uid)
        val targetRef = FirebaseDatabase.getInstance().reference.child("Capaian").child(firebaseUser.uid)
        val kunciRef = FirebaseDatabase.getInstance().reference.child("KunciCapaian").child(firebaseUser.uid)

        transaksiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalPemasukan = 0
                var totalPengeluaran = 0
                val formater = NumberFormat.getInstance()

                if (snapshot.exists()) {
                    for (tranSnap in snapshot.children) {
                        val tranData = tranSnap.getValue(Transaksi::class.java)
                        when (tranData?.kategori) {
                            "Pemasukan" -> totalPemasukan += tranData.jumlahSaldo ?: 0
                            "Pengeluaran" -> totalPengeluaran += tranData.jumlahSaldo ?: 0
                        }
                    }
                }

                val totalPemasukanRef = saldoRef.child("pemasukan")
                totalPemasukanRef.setValue(totalPemasukan)
                val forPemasukan = formater.format(totalPemasukan)
                binding.tvPemasukan.text = forPemasukan.toString()

                val totalPengeluaranRef = saldoRef.child("pengeluaran")
                totalPengeluaranRef.setValue(totalPengeluaran)
                val forPengeluaran = formater.format(totalPengeluaran)
                binding.tvPengeluaran.text = forPengeluaran.toString()

                targetRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(targetSnapshot: DataSnapshot) {
                        var totalSaldoTerkumpul = 0
                        for (targetItemSnapshot in targetSnapshot.children) {
                            val targetItemData = targetItemSnapshot.getValue(Target::class.java)
                            if (targetItemData != null && targetItemData.aktif == true && targetItemData.kunci == false) {
                                totalSaldoTerkumpul += targetItemData.saldoTerkumpul ?: 0
                            }
                        }

                        kunciRef.addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(kunciSnapshot: DataSnapshot) {
                                var totalSaldoKunci = 0
                                for (kunciSnap in kunciSnapshot.children) {
                                    val kunciItemData = kunciSnap.getValue(KunciCapaian::class.java)
                                    if(kunciItemData != null){
                                        totalSaldoKunci += kunciItemData.saldo ?: 0
                                    }
                                }
                                val totalSaldoAkhir = totalPemasukan - totalPengeluaran - totalSaldoTerkumpul - totalSaldoKunci
                                val totalSaldoRef = saldoRef.child("saldo")
                                totalSaldoRef.setValue(totalSaldoAkhir)

                                val forSaldo = formater.format(totalSaldoAkhir)
                                binding.tvSaldo.text = forSaldo.toString()
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "inflater.inflate(R.menu.toolbar_menu, menu)",
        "org.d3if2099.jagomenabung.R"
    ))
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.logout -> {
                AlertDialog.Builder(context).apply {
                    setMessage(R.string.logout)
                    setPositiveButton("YA") { _, _ ->
                        logoutUser()
                    }
                    setNegativeButton("Batal") { dialog, _ ->
                        dialog.cancel()
                    }
                    show()
                }
                true
            }
            R.id.target -> {
                findNavController().navigate(R.id.action_homeFragment_to_targetFragment)
                true
            }
            R.id.grafik -> {
                findNavController().navigate(R.id.action_homeFragment_to_grafikFragment)
                true
            }
            R.id.profile -> {
                findNavController().navigate(R.id.action_homeFragment_to_profile)
                true
            }
            R.id.filter -> {
                showFilterDialog()
                true
            }
            R.id.refresh -> {
                showTransaksi { transaksi ->
                    val groupedTransactions = groupTransactionsByDate(transaksi)
                    val groupedTransactionAdapter = AdapterGrupTransaksi(groupedTransactions, this)
                    recyclerView.adapter = groupedTransactionAdapter
                }
                Toast.makeText(requireContext(), "Refresh", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("InflateParams")
    private fun showFilterDialog(){
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_filter, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val judul = bottomSheetView.findViewById<EditText>(R.id.dialogJudulTIL)
        val tanggal = bottomSheetView.findViewById<EditText>(R.id.dialogTanggalTIL)
        val btnFilter = bottomSheetView.findViewById<Button>(R.id.btnFilter)
        val kategori = bottomSheetView.findViewById<AutoCompleteTextView>(R.id.dialogKategoriSpinner)
        val kat = resources.getStringArray(R.array.kategori_filter)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_kategori, kat)
        kategori.setAdapter(arrayAdapter)
        val bulan = binding.tvBulan.text.toString()

        tanggal.setOnClickListener {
            showDatePicker(tanggal, bulan)
        }

        btnFilter.setOnClickListener {
            val ta = tanggal.text.toString()
            if (ta.isNotEmpty()){
                val getJudul = judul.text.toString()
                val getTanggal = convertStringToTimestamp(ta)
                val getKategori = kategori.text.toString()
                showListTransaksiFilter(getJudul, getTanggal, getKategori) { transaksi ->
                    val groupedTransactions = groupTransactionsByDate(transaksi)
                    val groupedTransactionAdapter = AdapterGrupTransaksi(groupedTransactions, this)
                    recyclerView.adapter = groupedTransactionAdapter
                }
            } else {
                val getJudul = judul.text.toString()
                val getTanggal = 0L
                val getKategori = kategori.text.toString()
                showListTransaksiFilter(getJudul, getTanggal, getKategori) { transaksi ->
                    val groupedTransactions = groupTransactionsByDate(transaksi)
                    val groupedTransactionAdapter = AdapterGrupTransaksi(groupedTransactions, this)
                    recyclerView.adapter = groupedTransactionAdapter
                }
            }

            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showDatePicker(editText: EditText, bulanS: String) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val dateFor = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = dateFor.parse(bulanS)!!

        val bulan = cal.get(Calendar.MONTH)
        val tahun = cal.get(Calendar.YEAR)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDayOfMonth)
                if (selectedMonth == bulan && selectedYear == tahun) {
                    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                    val formattedDate = dateFormat.format(selectedDate.time)
                    editText.setText(formattedDate)
                } else {
                    Toast.makeText(requireContext(), "Pilih tanggal di bulan Agustus", Toast.LENGTH_SHORT).show()
                }
            },
            year, month, dayOfMonth
        )
        val dateStart = Calendar.getInstance()
        dateStart.set(tahun, bulan, 1)
        val dateEnd = Calendar.getInstance()
        dateEnd.set(tahun, bulan, dateEnd.getActualMaximum(Calendar.DAY_OF_MONTH))

        datePickerDialog.datePicker.minDate = dateStart.timeInMillis
        datePickerDialog.datePicker.maxDate = dateEnd.timeInMillis

        datePickerDialog.show()
    }

    private fun cekCapaian() {
        val capaianRef = FirebaseDatabase.getInstance().reference.child("Capaian").child(firebaseUser.uid)
        capaianRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentTimeMillis = System.currentTimeMillis()
                for (capaianSnapshot in snapshot.children) {
                    for (itemSnapshot in capaianSnapshot.children) {
                        val capaianId = itemSnapshot.child("id").getValue(String::class.java) ?: ""
                        val aktif = itemSnapshot.child("aktif").getValue(Boolean::class.java) ?: false
                        val tanggalBerakhir = itemSnapshot.child("tanggalBerakhir").getValue(Long::class.java) ?: 0
                        val target = itemSnapshot.child("jumlahSaldo").getValue(Int::class.java) ?: 0
                        val terkumpul = itemSnapshot.child("saldoTerkumpul").getValue(Int::class.java) ?: 0
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = tanggalBerakhir
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        if (aktif && calendar.timeInMillis < currentTimeMillis && terkumpul != target) {
                            capaianRef.child(capaianSnapshot.key ?: "").child(capaianId).child("aktif").setValue(false)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun convertStringToTimestamp(dateString: String): Long {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        return date?.time ?: 0
    }

    private fun logoutUser() {
        Firebase.auth.signOut()
        val intent = Intent (activity, LoginActivity::class.java)
        activity?.startActivity(intent)
        activity?.finish()
    }

    override fun onItemClick(transaksi: Transaksi, v: View) {
        val id = transaksi.id
        setFragmentResult(
            "id",
            bundleOf("id" to id)
        )
        val kategori = transaksi.kategori
        setFragmentResult(
            "kategori",
            bundleOf("kategori" to kategori)
        )
        val tanggal = transaksi.tanggal.toString()
        setFragmentResult(
            "tanggal",
            bundleOf("tanggal" to tanggal)
        )
        val judul = transaksi.judul
        setFragmentResult(
            "judul",
            bundleOf("judul" to judul)
        )
        val jumlahSaldo = transaksi.jumlahSaldo
        setFragmentResult(
            "jumlahSaldo",
            bundleOf("jumlahSaldo" to jumlahSaldo)
        )
        val keterangan = transaksi.keterangan
        setFragmentResult(
            "keterangan",
            bundleOf("keterangan" to keterangan)
        )
        findNavController().navigate(R.id.action_homeFragment_to_showTransaksiFragment)
    }
}