package com.smsexpensetracker

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.*

class ReportsFragment : Fragment() {
    
    private lateinit var viewModel: ReportsViewModel
    private lateinit var spinnerPeriod: Spinner
    private lateinit var pieChart: PieChart
    private lateinit var textTotalAmount: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reports, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[ReportsViewModel::class.java]
        
        initializeViews(view)
        setupPeriodSpinner()
        observeData()
        
        // Load current month data by default
        loadCurrentMonthData()
    }
    
    private fun initializeViews(view: View) {
        spinnerPeriod = view.findViewById(R.id.spinnerPeriod)
        pieChart = view.findViewById(R.id.pieChart)
        textTotalAmount = view.findViewById(R.id.textTotalAmount)
        
        setupPieChart()
    }
    
    private fun setupPeriodSpinner() {
        val periods = arrayOf("This Month", "Last Month", "Last 3 Months", "Last 6 Months", "This Year")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, periods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPeriod.adapter = adapter
        
        spinnerPeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> loadCurrentMonthData()
                    1 -> loadLastMonthData()
                    2 -> loadLast3MonthsData()
                    3 -> loadLast6MonthsData()
                    4 -> loadCurrentYearData()
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupPieChart() {
        pieChart.description.isEnabled = false
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true
        pieChart.animateXY(1000, 1000)
    }
    
    private fun observeData() {
        viewModel.categoryTotals.observe(viewLifecycleOwner) { categoryTotals ->
            updatePieChart(categoryTotals)
        }
        
        viewModel.totalAmount.observe(viewLifecycleOwner) { total ->
            textTotalAmount.text = "Total: ₹${String.format("%.2f", total)}"
        }
    }
    
    private fun updatePieChart(categoryTotals: List<CategoryTotal>) {
        if (categoryTotals.isEmpty()) {
            pieChart.clear()
            return
        }
        
        val entries = categoryTotals.map { categoryTotal ->
            PieEntry(categoryTotal.total.toFloat(), categoryTotal.category)
        }
        
        val dataSet = PieDataSet(entries, "Expenses by Category")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f
        
        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.invalidate()
    }
    
    private fun loadCurrentMonthData() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endOfMonth = calendar.timeInMillis
        
        viewModel.loadDataForPeriod(startOfMonth, endOfMonth)
    }
    
    private fun loadLastMonthData() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfLastMonth = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endOfLastMonth = calendar.timeInMillis
        
        viewModel.loadDataForPeriod(startOfLastMonth, endOfLastMonth)
    }
    
    private fun loadLast3MonthsData() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -3)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        
        val now = System.currentTimeMillis()
        viewModel.loadDataForPeriod(start, now)
    }
    
    private fun loadLast6MonthsData() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -6)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        
        val now = System.currentTimeMillis()
        viewModel.loadDataForPeriod(start, now)
    }
    
    private fun loadCurrentYearData() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfYear = calendar.timeInMillis
        
        val now = System.currentTimeMillis()
        viewModel.loadDataForPeriod(startOfYear, now)
    }
}