package com.d3if2099.jagomenabung.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
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
import com.d3if2099.jagomenabung.adapter.AdapterUserTransaksi
import com.d3if2099.jagomenabung.antarmuka.DateSelected
import com.d3if2099.jagomenabung.databinding.FragmentHomeBinding
import com.d3if2099.jagomenabung.model.Transaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class HomeFragment : Fragment(), DateSelected {
    private lateinit var binding : FragmentHomeBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterUserTransaksi
    private lateinit var firebaseUser : FirebaseUser
    private lateinit var ref: Query
    private lateinit var ref2: DatabaseReference
    private lateinit var ref3: DatabaseReference
    private val transaksiArrayList = arrayListOf<Transaksi>()

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


        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        adapter = AdapterUserTransaksi(arrayListOf(),object : AdapterUserTransaksi.onItemClicklistener{
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
                val tanggal = transaksi.tanggal
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
        })

        recyclerView.adapter = adapter

        binding.tvTanggal.setOnClickListener {
            showDatePicker()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_fragmentTambahTransaksi)
        }

        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    private fun currentDate(){
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("EEEE, dd MMM yyy")
        var currentDateAndTime: String = simpleDateFormat.format(calendar.time)

        binding.tvTanggal.text = currentDateAndTime
        checkAndLoadData()
        showList()

        binding.previous.setOnClickListener {
            calendar.add(Calendar.DATE, -1)
            currentDateAndTime = simpleDateFormat.format(calendar.time)
            binding.tvTanggal.text = currentDateAndTime
            checkAndLoadData()
            showList()
        }

        binding.next.setOnClickListener {
            calendar.add(Calendar.DATE, 1)
            currentDateAndTime = simpleDateFormat.format(calendar.time)
            binding.tvTanggal.text = currentDateAndTime
            checkAndLoadData()
            showList()
        }
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

        val viewFormatter = SimpleDateFormat("EEEE, dd MMM yyy")
        var viewFormattedDate : String = viewFormatter.format(calendar.time)

        binding.tvTanggal.text = viewFormattedDate

        binding.previous.setOnClickListener {
            calendar.add(Calendar.DATE, -1)
            viewFormattedDate = viewFormatter.format(calendar.time)
            binding.tvTanggal.text = viewFormattedDate
            checkAndLoadData()
            showList()
        }

        binding.next.setOnClickListener {
            calendar.add(Calendar.DATE, 1)
            viewFormattedDate = viewFormatter.format(calendar.time)
            binding.tvTanggal.text = viewFormattedDate
            checkAndLoadData()
            showList()
        }
        checkAndLoadData()
        showList()
    }

    private fun showList() {
        val tanggalS = binding.tvTanggal.text.toString()
        val tanggal = convertStringToDate(tanggalS)
        ref = FirebaseDatabase.getInstance().reference.child("Transaksi").child(firebaseUser.uid).orderByChild("tanggal").equalTo(tanggal.toString())
        ref.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                transaksiArrayList.clear()
                if(snapshot.exists()){
                    for(tranSnap in snapshot.children) {
                        val tranData = tranSnap.getValue(Transaksi::class.java)
                        transaksiArrayList.add(tranData!!)
                    }
                    binding.nullable.visibility = View.GONE
                    adapter.setData(transaksiArrayList)
                }else {
                    adapter.setData(transaksiArrayList)
                    binding.nullable.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateTotalSaldo() {
        val tanggalS = binding.tvTanggal.text.toString()
        val tanggal = convertStringToDate(tanggalS).toString()
        val timestamp = convertStringToTimestamp(tanggal)

        val saldoRef = ref2.child(firebaseUser.uid).child(timestamp.toString())
        val transaksiRef = FirebaseDatabase.getInstance().reference.child("Transaksi").child(firebaseUser.uid)

        transaksiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalPemasukan = 0
                var totalPengeluaran = 0
                val formater = NumberFormat.getInstance()

                if (snapshot.exists()){
                    for (tranSnap in snapshot.children) {
                        val tranData = tranSnap.getValue(Transaksi::class.java)
                        if (tranData?.tanggal == tanggal) {
                            when (tranData.kategori) {
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

                    val iSaldo = totalPemasukan - totalPengeluaran
                    val forSaldo = formater.format(iSaldo)

                    binding.tvSaldo.text = forSaldo.toString()

                    val totalSaldoRef = saldoRef.child("saldo")
                    totalSaldoRef.setValue(iSaldo)
                } else {
                    binding.tvPemasukan.text = "0"
                    binding.tvPengeluaran.text = "0"
                    binding.tvSaldo.text = "0"
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkAndLoadData() {
        val tanggalS = binding.tvTanggal.text.toString()
        val tanggal = convertStringToDate(tanggalS).toString()
        val transaksiRef = FirebaseDatabase.getInstance().reference.child("Transaksi").child(firebaseUser.uid)

        transaksiRef.orderByChild("tanggal").equalTo(tanggal).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Jika ada transaksi pada tanggal yang baru, maka panggil updateTotalSaldo()
                if (snapshot.exists()) {
                    updateTotalSaldo()
                } else {

                    // Update tampilan dengan nilai 0
                    binding.tvPemasukan.text = "0"
                    binding.tvPengeluaran.text = "0"
                    binding.tvSaldo.text = "0"
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun semuaTransaksi(){
        val tanggalS = binding.tvTanggal.text.toString()
        val tanggal = convertStringToDate(tanggalS)
        ref = FirebaseDatabase.getInstance().reference.child("Transaksi").child(firebaseUser.uid).orderByChild("tanggal").equalTo(tanggal.toString())
        ref.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                transaksiArrayList.clear()
                if(snapshot.exists()){
                    for(tranSnap in snapshot.children) {
                        val tranData = tranSnap.getValue(Transaksi::class.java)
                        transaksiArrayList.add(tranData!!)
                    }
                    binding.nullable.visibility = View.GONE
                    adapter.setData(transaksiArrayList)
                } else {
                    adapter.setData(transaksiArrayList)
                    binding.nullable.visibility = View.VISIBLE
                }
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
            R.id.pemasukan -> {
                listPemasukan()
                true
            }
            R.id.pengeluaran -> {
                listPengeluaran()
                true
            }
            R.id.semua -> {
                semuaTransaksi()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun listPengeluaran() {
        val tanggalS = binding.tvTanggal.text.toString()
        val tanggal = convertStringToDate(tanggalS)
        ref = FirebaseDatabase.getInstance().reference.child("Transaksi").child(firebaseUser.uid).orderByChild("tanggal").equalTo(tanggal.toString())
        ref.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                transaksiArrayList.clear()
                if(snapshot.exists()){
                    for(tranSnap in snapshot.children) {
                        val tranData = tranSnap.getValue(Transaksi::class.java)
                        val kategori = tranData!!.kategori
                        if (kategori.toString() == "Pengeluaran"){
                            transaksiArrayList.add(tranData)
                        }
                    }
                    binding.nullable.visibility = View.GONE
                    adapter.setData(transaksiArrayList)
                }else {
                    adapter.setData(transaksiArrayList)
                    binding.nullable.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun listPemasukan() {
        val tanggalS = binding.tvTanggal.text
        val tanggal = convertStringToDate(tanggalS as String)
        ref = FirebaseDatabase.getInstance().reference.child("Transaksi").child(firebaseUser.uid).orderByChild("tanggal").equalTo(tanggal.toString())
        ref.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                transaksiArrayList.clear()
                if(snapshot.exists()){
                    for(tranSnap in snapshot.children) {
                        val tranData = tranSnap.getValue(Transaksi::class.java)
                        val kategori = tranData!!.kategori
                        if (kategori.toString() == "Pemasukan"){
                            transaksiArrayList.add(tranData)
                        }
                    }
                    binding.nullable.visibility = View.GONE
                    adapter.setData(transaksiArrayList)
                }else {
                    adapter.setData(transaksiArrayList)
                    binding.nullable.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    @SuppressLint("NewApi")
    fun convertStringToDate(inputDate: String): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("EEEE, dd MMM yyy")
        return LocalDate.parse(inputDate, formatter)
    }

    private fun convertStringToTimestamp(dateString: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.parse(dateString)
        return date?.time ?: 0
    }

    private fun logoutUser() {
        Firebase.auth.signOut()
        val intent = Intent (activity, LoginActivity::class.java)
        activity?.startActivity(intent)
        activity?.finish()
    }

}