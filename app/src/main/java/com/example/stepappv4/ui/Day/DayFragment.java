package com.example.stepappv4.ui.Day;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.example.stepappv4.StepAppOpenHelper;
import com.example.stepappv4.R;
import com.example.stepappv4.databinding.FragmentDayBinding;
import com.example.stepappv4.databinding.FragmentReportBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DayFragment extends Fragment {

    private TextView stepsTextView;
    private FragmentDayBinding binding;
    private AnyChartView anyChartView;
    private Map<String, Integer> stepsByDate;  // Map to hold steps by date

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDayBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize AnyChartView and ProgressBar for loading
        anyChartView = root.findViewById(R.id.dayBarChart);
        anyChartView.setProgressBar(root.findViewById(R.id.loadingBar));

        // Generate and display the column chart
        Cartesian cartesian = createColumnChart();
        anyChartView.setBackgroundColor("#00000000");
        anyChartView.setChart(cartesian);

        return root;
    }

    // Method to create and return a column chart
    private Cartesian createColumnChart() {
        // Retrieve steps by date for a range of dates
        stepsByDate = StepAppOpenHelper.loadStepsByDateRange(getContext());

        // Create a TreeMap to hold all the days of the week with default step count of 0
        Map<String, Integer> graphMap = new TreeMap<>();

        // Get the current date and calculate the start and end of the week
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Format should match your database
        for (int i = 0; i < 7; i++) { // Iterate for the past week
            Date date = new Date(currentDate.getTime() - (i * 24 * 60 * 60 * 1000)); // Subtract days
            String formattedDate = dateFormat.format(date);
            graphMap.put(formattedDate, 0); // Initialize with 0 steps
        }

        // Update the graphMap with actual steps data from the database
        if (stepsByDate != null) {
            for (Map.Entry<String, Integer> entry : stepsByDate.entrySet()) {
                if (graphMap.containsKey(entry.getKey())) {
                    graphMap.put(entry.getKey(), entry.getValue()); // Update the count if data exists
                }
            }
        }

        // Create a Cartesian chart for column chart
        Cartesian cartesian = AnyChart.column();

        // Prepare data entries for the chart
        List<DataEntry> data = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : graphMap.entrySet()) {
            data.add(new ValueDataEntry(entry.getKey(), entry.getValue()));
        }

        // Set up the column series and style
        Column column = cartesian.column(data);
        column.fill("#1EB980");
        column.stroke("#1EB980");

        // Set tooltip for columns
        column.tooltip()
                .titleFormat("Date: {%X}")
                .format("{%Value} Steps")
                .anchor(Anchor.RIGHT_BOTTOM)
                .position(Position.RIGHT_TOP)
                .offsetX(0d)
                .offsetY(5);

        // Set cartesian properties and UI enhancements
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);
        cartesian.yScale().minimum(0);
        cartesian.yAxis(0).title("Number of Steps");
        cartesian.xAxis(0).title("Date");

        // Ensure all dates are displayed on the x-axis
        cartesian.xAxis(0).labels().format("{%Value}"); // Display the date labels
        cartesian.xAxis(0).labels().padding(10); // Adjust padding for visibility
        cartesian.xAxis(0).ticks().enabled(true); // Enable ticks to separate dates

        // Optional: Set the x-axis to fit all dates evenly
        cartesian.xAxis(0).title().padding(10); // Adjust the title padding if needed

        cartesian.background().fill("#00000000");
        cartesian.animation(true);

        return cartesian;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
