package pt.uninova.s4h.citizenhub.ui.summary;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyBloodPressurePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyCaloriesPanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyDistancePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyHeartRatePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyPosturePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyStepsPanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyBloodPressurePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyCaloriesPanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyDistancePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyHeartRatePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyPosturePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyStepsPanel;

public class ChartFunctions {

    private final Context context;
    private final LocalDate localDate;

    public ChartFunctions(Context context, LocalDate localDate) {
        this.context = context;
        this.localDate = localDate;
    }

    // This section has functions used to parse different utils to a more generic one used to pass the data to the charts //
    public TwoDimensionalChartData parseBloodPressureUtil(List<HourlyBloodPressurePanel> hourlyBloodPressurePanels){
        TwoDimensionalChartData twoDimensionalChartData = new TwoDimensionalChartData(24, 3);
        for (HourlyBloodPressurePanel data : hourlyBloodPressurePanels){
            twoDimensionalChartData.set(data.getHourOfDay(), 0, data.getSystolic());
            twoDimensionalChartData.set(data.getHourOfDay(), 1, data.getDiastolic());
            twoDimensionalChartData.set(data.getHourOfDay(), 2, data.getMean());
        }
        return twoDimensionalChartData;
    }

    public TwoDimensionalChartData parseBloodPressureUtil(List<DailyBloodPressurePanel> dailyBloodPressurePanels, int days){
        TwoDimensionalChartData twoDimensionalChartData = new TwoDimensionalChartData(days, 3);
        for (DailyBloodPressurePanel data : dailyBloodPressurePanels){
            twoDimensionalChartData.set(data.getDay(), 0, data.getSystolic());
            twoDimensionalChartData.set(data.getDay(), 1, data.getDiastolic());
            twoDimensionalChartData.set(data.getDay(), 2, data.getMean());
        }
        return twoDimensionalChartData;
    }

    public TwoDimensionalChartData parseCaloriesUtil(List<HourlyCaloriesPanel> hourlyCaloriesPanels){
        TwoDimensionalChartData twoDimensionalChartData = new TwoDimensionalChartData(24, 1);
        for (HourlyCaloriesPanel dailyCaloriesPanel : hourlyCaloriesPanels){
            twoDimensionalChartData.set(dailyCaloriesPanel.getHourOfDay(), 0, dailyCaloriesPanel.getCalories());
        }
        return twoDimensionalChartData;
    }
    public TwoDimensionalChartData parseCaloriesUtil(List<DailyCaloriesPanel> dailyCaloriesPanels, int days){
        TwoDimensionalChartData twoDimensionalChartData = new TwoDimensionalChartData(days, 1);
        for (DailyCaloriesPanel dailyCaloriesPanel : dailyCaloriesPanels){
            twoDimensionalChartData.set(dailyCaloriesPanel.getDay(), 0, dailyCaloriesPanel.getCalories());
        }
        return twoDimensionalChartData;
    }

    public TwoDimensionalChartData parseDistanceUtil(List<HourlyDistancePanel> hourlyDistancePanels){
        TwoDimensionalChartData twoDimensionalChartData = new TwoDimensionalChartData(24, 1);
        for (HourlyDistancePanel hourlyDistancePanel : hourlyDistancePanels){
            twoDimensionalChartData.set(hourlyDistancePanel.getHourOfDay(), 0, hourlyDistancePanel.getDistance());
        }
        return twoDimensionalChartData;
    }
    public TwoDimensionalChartData parseDistanceUtil(List<DailyDistancePanel> dailyDistancePanels, int days){
        TwoDimensionalChartData twoDimensionalChartData = new TwoDimensionalChartData(days, 1);
        for (DailyDistancePanel dailyCaloriesPanel : dailyDistancePanels){
            twoDimensionalChartData.set(dailyCaloriesPanel.getDay(), 0, dailyCaloriesPanel.getDistance());
        }
        return twoDimensionalChartData;
    }

    public TwoDimensionalChartData parseHeartRateUtil(List<HourlyHeartRatePanel> hourlyHeartRatePanels){
        TwoDimensionalChartData twoDimensionalChartData = new TwoDimensionalChartData(24, 3);
        for (HourlyHeartRatePanel data : hourlyHeartRatePanels){
            twoDimensionalChartData.set(data.getHourOfDay(), 0, data.getAverage());
            twoDimensionalChartData.set(data.getHourOfDay(), 1, data.getMaximum());
            twoDimensionalChartData.set(data.getHourOfDay(), 2, data.getMinimum());
        }
        return twoDimensionalChartData;
    }

    public TwoDimensionalChartData parseHeartRateUtil(List<DailyHeartRatePanel> dailyHeartRatePanels, int days){
        TwoDimensionalChartData twoDimensionalChartData = new TwoDimensionalChartData(days, 3);
        for (DailyHeartRatePanel data : dailyHeartRatePanels){
            twoDimensionalChartData.set(data.getDay(), 0, data.getAverage());
            twoDimensionalChartData.set(data.getDay(), 1, data.getMaximum());
            twoDimensionalChartData.set(data.getDay(), 2, data.getMinimum());
        }
        return twoDimensionalChartData;
    }

    public TwoDimensionalChartData parseStepsUtil(List<HourlyStepsPanel> hourlyStepsPanels){
        TwoDimensionalChartData twoDimensionalChartData = new TwoDimensionalChartData(24, 1);
        for (HourlyStepsPanel hourlyStepsPanel : hourlyStepsPanels){
            twoDimensionalChartData.set(hourlyStepsPanel.getHourOfDay(), 0, hourlyStepsPanel.getSteps());
        }
        return twoDimensionalChartData;
    }
    public TwoDimensionalChartData parseStepsUtil(List<DailyStepsPanel> dailyStepsPanels, int days){
        TwoDimensionalChartData twoDimensionalChartData = new TwoDimensionalChartData(days, 1);
        for (DailyStepsPanel dailyStepsPanel : dailyStepsPanels){
            twoDimensionalChartData.set(dailyStepsPanel.getDay(), 0, dailyStepsPanel.getSteps());
        }
        return twoDimensionalChartData;
    }

    public TwoDimensionalChartData parsePostureUtil(List<HourlyPosturePanel> hourlyPosturePanels){
        TwoDimensionalChartData twoDimensionalChartData = new TwoDimensionalChartData(24, 2);
        for (HourlyPosturePanel hourlyPosturePanel : hourlyPosturePanels) {
            twoDimensionalChartData.set(hourlyPosturePanel.getHourOfDay(), 0, hourlyPosturePanel.getCorrectPosture());
            twoDimensionalChartData.set(hourlyPosturePanel.getHourOfDay(), 1, hourlyPosturePanel.getIncorrectPosture());
        }
        return twoDimensionalChartData;
    }

    public TwoDimensionalChartData parsePostureUtil(List<DailyPosturePanel> dailyPosturePanels, int days){
        TwoDimensionalChartData twoDimensionalChartData = new TwoDimensionalChartData(days, 2);
        for (DailyPosturePanel data : dailyPosturePanels) {
            twoDimensionalChartData.set(data.getDay(), 0, data.getCorrectPosture());
            twoDimensionalChartData.set(data.getDay(), 1, data.getIncorrectPosture());
        }
        return twoDimensionalChartData;
    }
    //********************************************************************************************************************//

    // This section has functions used to define some characteristics of the different charts that cannot done in the layout //
    public void setupBarChart(BarChart barChart, ChartMarkerView chartMarkerView) {
        barChart.setDrawGridBackground(false);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setMarker(chartMarkerView);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setDrawGridLines(false);

        YAxis yAxisRight = barChart.getAxisRight();
        yAxisRight.setEnabled(false);
    }

    public void setupLineChart(LineChart lineChart, ChartMarkerView chartMarkerView) {
        lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setMarker(chartMarkerView);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(24);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setDrawGridLines(false);

        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);
    }

    public void setupPieChart(PieChart pieChart) {
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
    }
    //***********************************************************************************************************************//

    /* setLabels: used to get the labels for the charts
    * Parameters:
    * max (int) - it represents the maximum of the X axis
    * Return:
    * labels (String) - a string that contains the labels which will be displayed in the charts */
    public String[] setLabels(int days) {
        String[] labels = new String[days];
        if(days == 24)
            labels = new String[days + 1];
        int i = 0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(Date.from(localDate.minusDays(days - 1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        if (days == 24) {
            while(i <= days) {
                labels[i] = String.valueOf(i);
                i++;
            }
        } else if (days == 7) {
            while (i < days) {
                labels[i] = Objects.requireNonNull(cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())).substring(0, 3);
                cal.add(Calendar.DATE, + 1);
                i++;
            }
        } else {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
            while (i < days) {
                labels[i] = sdf.format(cal.getTime());
                cal.add(Calendar.DATE, + 1);
                i++;
            }
        }
        return labels;
    }
    // This sections has functions used to input data into the different charts //
    /* setBarChartData: used to plot the data on a bar chart
    * Parameters:
    * list (List<SummaryDetailUtil>) - the data retrieved from the queries
    * barChart (BarChart) - a barChart
    * label (String) - the label of the information to be displayed
    * days (int) - represents the number of days to add to the chart. If it is 24, it equivalent to one day */
    public void setBarChartData(BarChart barChart, TwoDimensionalChartData twoDimensionalChartData, String label, int days) {
        List<BarEntry> entries = new ArrayList<>();

        for (int y = 0; y < twoDimensionalChartData.getY(); y++) {
            for (int x = 0; x < twoDimensionalChartData.getX(); x++) {
                entries.add(new BarEntry((float) x, (float) twoDimensionalChartData.get(x, y)));
            }
        }

        if(days == 24)
            entries.add(new BarEntry(24, 0));

        BarDataSet barDataSet = new BarDataSet(entries, label);
        barDataSet.setColor(ContextCompat.getColor(context, R.color.colorS4HLightBlue));
        barDataSet.setDrawValues(false);

        ArrayList<IBarDataSet> dataSet = new ArrayList<>();
        dataSet.add(barDataSet);

        BarData barData = new BarData(dataSet);
        barData.setValueFormatter(new ChartValueFormatter());
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(setLabels(days)));
        barChart.getDescription().setText(label);
        barChart.invalidate();
    }

    private void setStackedBar(BarChart barChart, TwoDimensionalChartData twoDimensionalChartData, String[] labels, int max){
        float[][] values = new float[twoDimensionalChartData.getX()][twoDimensionalChartData.getY()];

        for(int y = 0; y < twoDimensionalChartData.getY(); y++){
            for (int x = 0; x < twoDimensionalChartData.getX(); x++){
                values[x][y] = (float) twoDimensionalChartData.get(x, y);
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        for (int x = 0; x < twoDimensionalChartData.getX(); x++){
            entries.add(new BarEntry((float) x, values[x]));
        }

        BarDataSet barDataSet = new BarDataSet(entries, null);
        barDataSet.setColors(ContextCompat.getColor(context, R.color.colorS4HLightBlue), ContextCompat.getColor(context, R.color.colorS4HOrange));
        barDataSet.setStackLabels(labels);

        ArrayList<IBarDataSet> dataSet = new ArrayList<>();
        dataSet.add(barDataSet);

        BarData barData = new BarData(dataSet);
        barData.setValueFormatter(new ChartValueFormatter());

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(setLabels(max)));
        //barChart.groupBars(0.2f, 0.25f, 0.05f);
        barChart.invalidate();
    }

    public void setLineChartData(LineChart lineChart, TwoDimensionalChartData twoDimensionalChartData, String[] label, int days) {
        ArrayList<ILineDataSet> dataSet = new ArrayList<>();

        for (int y = 0; y < twoDimensionalChartData.getY(); y++) {
            List<Entry> entries = new ArrayList<>();
            for (int x = 0; x < twoDimensionalChartData.getX(); x++){
                if (twoDimensionalChartData.get(x, y) == 0)
                    continue;
                entries.add(new BarEntry((float) x, (float) twoDimensionalChartData.get(x, y)));
            }
            LineDataSet lineDataSet = getLineDataSet(entries, label[y], y);
            dataSet.add(lineDataSet);
        }

        LineData lineData = new LineData(dataSet);
        lineData.setValueFormatter(new ChartValueFormatter());
        lineChart.setData(lineData);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(setLabels(days)));
        lineChart.invalidate();
    }

    private LineDataSet getLineDataSet(List<Entry> entries, String label, int color){
        if (entries.size() < 1)
            return null;

        LineDataSet lineDataSet = new LineDataSet(entries, label);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawCircleHole(true);
        switch (color) {
            case 0:
                lineDataSet.setColor(ContextCompat.getColor(context, R.color.colorS4HLightBlue));
                lineDataSet.setCircleColor(ContextCompat.getColor(context, R.color.colorS4HLightBlue));
                lineDataSet.setCircleHoleColor(ContextCompat.getColor(context, R.color.colorS4HLightBlue));
                break;
            case 1:
                lineDataSet.setColor(ContextCompat.getColor(context, R.color.colorS4HOrange));
                lineDataSet.setCircleColor(ContextCompat.getColor(context, R.color.colorS4HOrange));
                lineDataSet.setCircleHoleColor(ContextCompat.getColor(context, R.color.colorS4HOrange));
                break;
            case 2:
                lineDataSet.setColor(ContextCompat.getColor(context, R.color.colorS4HTurquoise));
                lineDataSet.setCircleColor(ContextCompat.getColor(context, R.color.colorS4HTurquoise));
                lineDataSet.setCircleHoleColor(ContextCompat.getColor(context, R.color.colorS4HTurquoise));
                break;
        }
        return lineDataSet;
    }

    public void setAreaChart(LineChart lineChart, TwoDimensionalChartData twoDimensionalChartData, String[] labels, int days){
        int currentTime = 0;
        double total;
        List<Entry> entries1 = new ArrayList<>();
        List<Entry> entries2 = new ArrayList<>();

        while(currentTime < days){
            total = twoDimensionalChartData.get(currentTime, 0) + twoDimensionalChartData.get(currentTime, 1);
            if(days == 24) {
                if (total > 3600000) {
                    twoDimensionalChartData.set(currentTime, 0, twoDimensionalChartData.get(currentTime, 0) * 3600000 / total);
                    twoDimensionalChartData.set(currentTime, 1, twoDimensionalChartData.get(currentTime, 1) * 3600000 / total);
                }
                entries1.add(new BarEntry(currentTime, (float) (twoDimensionalChartData.get(currentTime, 0) * 100 / 3600000)));
                entries2.add(new BarEntry(currentTime, (float) (twoDimensionalChartData.get(currentTime, 1) * 100 / 3600000)));
            } else {
                if (total > 86400000) {
                    twoDimensionalChartData.set(currentTime, 0, twoDimensionalChartData.get(currentTime, 0) * 86400000 / total);
                    twoDimensionalChartData.set(currentTime, 1, twoDimensionalChartData.get(currentTime, 1) * 86400000 / total);
                }
                entries1.add(new BarEntry(currentTime, (float) (twoDimensionalChartData.get(currentTime, 0) * 100 / 86400000)));
                entries2.add(new BarEntry(currentTime, (float) (twoDimensionalChartData.get(currentTime, 1) * 100 / 86400000)));
            }
            currentTime++;
        }
        LineDataSet lineDataSet1 = setLineDataSet(entries1, labels[0], ContextCompat.getColor(context, R.color.colorS4HLightBlue));
        LineDataSet lineDataSet2 = setLineDataSet(entries2, labels[1], ContextCompat.getColor(context, R.color.colorS4HOrange));

        ArrayList<ILineDataSet> dataSet = new ArrayList<>();
        dataSet.add(lineDataSet2);
        dataSet.add(lineDataSet1);

        LineData lineData = new LineData(dataSet);
        lineData.setValueFormatter(new ChartValueFormatter());

        lineChart.setData(lineData);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(setLabels(days)));
        lineChart.invalidate();
    }

    private LineDataSet setLineDataSet(List<Entry> entries, String label, int color){
        LineDataSet lineDataSet = new LineDataSet(entries, label);
        lineDataSet.setColor(color);
        lineDataSet.setFillColor(color);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillAlpha(255);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        return lineDataSet;
    }

    public void setPieChart(PieChart pieChart, TwoDimensionalChartData twoDimensionalChartData){
        int value1 = 0;
        int value2 = 0;

        int pos = 0;
        while(pos < 24)
        {
            value1 += twoDimensionalChartData.get(pos, 0);
            value2 += twoDimensionalChartData.get(pos, 1);
            pos++;
        }

        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(value1, secondsToString(value1 / 1000)));
        pieEntries.add(new PieEntry(value2, secondsToString(value2 / 1000)));
        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setDrawValues(false);
        dataSet.setColors(ContextCompat.getColor(context, R.color.colorS4HLightBlue), ContextCompat.getColor(context, R.color.colorS4HOrange));
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private String secondsToString(long value) {
        long seconds = value;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (minutes > 0)
            seconds = seconds % 60;

        if (hours > 0) {
            minutes = minutes % 60;
        }

        String result = ((hours > 0 ? hours + "h " : "") + (minutes > 0 ? minutes + "m " : "") + (seconds > 0 ? seconds + "s" : "")).trim();

        return result.equals("") ? "0s" : result;
    }

}
