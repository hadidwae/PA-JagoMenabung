package com.d3if2099.jagomenabung.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.d3if2099.jagomenabung.R
import com.d3if2099.jagomenabung.antarmuka.DateSelectedMonth
import com.d3if2099.jagomenabung.databinding.FragmentGrafikBinding
import com.d3if2099.jagomenabung.model.Transaksi
import java.text.DateFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class GrafikFragment : Fragment(), DateSelectedMonth {
    private lateinit var binding: FragmentGrafikBinding
    private lateinit var pieChart: PieChart
    private lateinit var auth : FirebaseAuth
    private lateinit var firebaseUser : FirebaseUser
    private lateinit var ref: DatabaseReference
    private var totalSaldoPemasukan : Float = 0.0f
    private var totalSaldoPengeluaran : Float = 0.0f
    private val colors: ArrayList<Int> = ArrayList()
    val entries: ArrayList<PieEntry> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGrafikBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        ref = FirebaseDatabase.getInstance().reference.child("TotalSaldo")

        binding.tvBulan.setOnClickListener {
            showMonthYearPicker()
        }

        currentDate()

        return binding.root
    }

    private fun loadChart() {
        pieChart = binding.pieChart

        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        pieChart.dragDecelerationFrictionCoef = 0.95f

        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)

        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)

        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f

        pieChart.setDrawCenterText(true)

        pieChart.rotationAngle = 0f

        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true

        pieChart.animateY(1400, Easing.EaseInOutQuad)

        pieChart.legend.isEnabled = false
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)

        colors.add(resources.getColor(R.color.pemasukan))
        colors.add(resources.getColor(R.color.pengeluaran))

        pieChart.highlightValues(null)

        pieChart.invalidate()

        val tvBulan = binding.tvBulan.text.toString()
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = dateFormat.parse(tvBulan)!!

        val bulan = cal.get(Calendar.MONTH)
        val tahun = cal.get(Calendar.YEAR)

        totalSaldoPemasukan = 0.0f
        totalSaldoPengeluaran = 0.0f

        ref = FirebaseDatabase.getInstance().reference.child("Transaksi").child(firebaseUser.uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                entries.clear()
                if(snapshot.exists() && snapshot.childrenCount > 0){
                    for (tranSnap in snapshot.children) {
                        val tranData = tranSnap.getValue(Transaksi::class.java)
                        if (tranData != null) {
                            val transactionCal = Calendar.getInstance()
                            transactionCal.timeInMillis = tranData.tanggal!!
                            val transactionMonth = transactionCal.get(Calendar.MONTH)
                            val transactionYear = transactionCal.get(Calendar.YEAR)
                            if (transactionMonth == bulan && transactionYear == tahun) {
                                when (tranData.kategori) {
                                    "Pemasukan" -> totalSaldoPemasukan += tranData.jumlahSaldo ?: 0
                                    "Pengeluaran" -> totalSaldoPengeluaran += tranData.jumlahSaldo ?: 0
                                }
                            } else {
                                entries.clear()
                            }
                        }
                    }

                    entries.add(PieEntry(totalSaldoPemasukan))
                    entries.add(PieEntry(totalSaldoPengeluaran))

                    val formatter = NumberFormat.getCurrencyInstance(Locale("in","ID"))
                    val saldoPemasukan = formatter.format(totalSaldoPemasukan.toString().toDouble())
                    val saldoPengeluaran = formatter.format(totalSaldoPengeluaran.toString().toDouble())

                    binding.tvSaldoPemasukan.text = saldoPemasukan.toString()
                    binding.tvSaldoPengeluaran.text = saldoPengeluaran.toString()

                    val dataSet = PieDataSet(entries, "Transaksi")
                    dataSet.setDrawIcons(false)
                    dataSet.sliceSpace = 3f
                    dataSet.iconsOffset = MPPointF(0f, 40f)
                    dataSet.selectionShift = 5f
                    dataSet.colors = colors

                    val data = PieData(dataSet)
                    data.setValueFormatter(PercentFormatter())
                    data.setValueTextSize(15f)
                    data.setValueTypeface(Typeface.DEFAULT_BOLD)
                    data.setValueTextColor(Color.WHITE)
                    pieChart.data = data

                } else {
                    entries.clear()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

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
    private fun currentDate(){
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("MMMM yyyy")
        var currentDateAndTime: String = simpleDateFormat.format(calendar.time)

        binding.tvBulan.text = currentDateAndTime
        loadChart()

        binding.previous.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            currentDateAndTime = simpleDateFormat.format(calendar.time)
            binding.tvBulan.text = currentDateAndTime
            loadChart()
        }

        binding.next.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            currentDateAndTime = simpleDateFormat.format(calendar.time)
            binding.tvBulan.text = currentDateAndTime
            loadChart()
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
        loadChart()

        binding.previous.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            viewFormattedDate = viewFormatter.format(calendar.time)
            binding.tvBulan.text = viewFormattedDate
            loadChart()
        }

        binding.next.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            viewFormattedDate = viewFormatter.format(calendar.time)
            binding.tvBulan.text = viewFormattedDate
            loadChart()
        }
    }
}