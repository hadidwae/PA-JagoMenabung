package com.d3if2099.jagomenabung.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
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
import com.d3if2099.jagomenabung.antarmuka.DateSelected
import com.d3if2099.jagomenabung.databinding.FragmentGrafikBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class GrafikFragment : Fragment(), DateSelected {
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

        binding.tvTanggal.setOnClickListener {
            showDatePicker()
        }

        currentDate()

        return binding.root
    }

    private fun loadChart() {
        val tanggalS = binding.tvTanggal.text.toString()
        val tanggalA = convertStringToDate(tanggalS).toString()
        val tanggal = convertStringToTimestamp(tanggalA)
        ref = FirebaseDatabase.getInstance().reference.child("TotalSaldo").child(firebaseUser.uid).child(tanggal.toString())
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                entries.clear()
                if(snapshot.exists()){
                    if (snapshot.hasChild("pemasukan")){
                        val sPem = snapshot.child("pemasukan").value.toString()
                        totalSaldoPemasukan = sPem.toFloat()

                    }
                    if (snapshot.hasChild("pengeluaran")){
                        val sPen = snapshot.child("pengeluaran").value.toString()
                        totalSaldoPengeluaran = sPen.toFloat()
                    }

                    entries.add(PieEntry(totalSaldoPemasukan))
                    entries.add(PieEntry(totalSaldoPengeluaran))

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

                    binding.tvVisible.visibility = View.GONE
                } else {
                    entries.clear()
                    binding.tvVisible.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    @SuppressLint("SimpleDateFormat")
    private fun currentDate(){
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("EEEE, dd MMM yyy")
        var currentDateAndTime: String = simpleDateFormat.format(calendar.time)

        binding.tvTanggal.text = currentDateAndTime
        loadChart()

        binding.previous.setOnClickListener {
            calendar.add(Calendar.DATE, -1)
            currentDateAndTime = simpleDateFormat.format(calendar.time)
            binding.tvTanggal.text = currentDateAndTime
            loadChart()
        }

        binding.next.setOnClickListener {
            calendar.add(Calendar.DATE, 1)
            currentDateAndTime = simpleDateFormat.format(calendar.time)
            binding.tvTanggal.text = currentDateAndTime
            loadChart()
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
            loadChart()
        }

        binding.next.setOnClickListener {
            calendar.add(Calendar.DATE, 1)
            viewFormattedDate = viewFormatter.format(calendar.time)
            binding.tvTanggal.text = viewFormattedDate
            loadChart()
        }

        loadChart()
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
}