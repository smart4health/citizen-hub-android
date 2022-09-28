package pt.uninova.s4h.citizenhub.ui.summary;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.persistence.entity.util.ActivityDetailUtil;
import pt.uninova.s4h.citizenhub.persistence.repository.CaloriesSnapshotMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.DistanceSnapshotMeasurementRepository;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;
import pt.uninova.s4h.citizenhub.persistence.repository.StepsSnapshotMeasurementRepository;

public class SummaryDetailActivityFragment extends Fragment {

    private SummaryViewModel model;
    private LineChart lineChart;
    private BarChart barChart;
    private BottomNavigationView bottomNavigationViewTime;
    private BottomNavigationView bottomNavigationViewActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(SummaryViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary_detail_activity, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barChart = requireView().findViewById(R.id.bar_chart);
        lineChart = requireView().findViewById(R.id.line_chart);

        bottomNavigationViewActivity = requireView().findViewById(R.id.nav_view_activity);
        bottomNavigationViewActivity.setOnNavigationItemSelectedListener(this::onNavigationItemSelectedActivity);

        bottomNavigationViewTime = requireView().findViewById(R.id.nav_view_time);
        bottomNavigationViewTime.setOnNavigationItemSelectedListener(this::onNavigationItemSelectedTime);

        barChart.setVisibility(View.INVISIBLE);
        setupBarChart();
        setupLineChart();
        dailySteps();

    }

    /*
     * Bar chart initial definitions.
     * Some definitions are altered depending on the information being displayed.
     * Those definitions are changed in the other functions.
     * */
    private void setupBarChart() {
        barChart.setDrawGridBackground(false);
        barChart.setFitBars(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setAxisMinimum(1);
        xAxis.setDrawGridLines(false);

        YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setDrawGridLines(false);

        YAxis yAxisRight = barChart.getAxisRight();
        yAxisRight.setEnabled(false);
    }

    /*
     * Same as the setupBarChart().
     * */
    private void setupLineChart() {
        lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setText("Steps");

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(24);
        xAxis.setDrawGridLines(false);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setDrawGridLines(false);

        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);
    }

    /*
     *
     * */
    @SuppressLint("NonConstantResourceId")
    private boolean onNavigationItemSelectedTime(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_day:
                System.out.println("Day");
                barChart.setVisibility(View.INVISIBLE);
                lineChart.setVisibility(View.VISIBLE);
                lineChart.getXAxis().setAxisMaximum(24);
                switch (bottomNavigationViewActivity.getSelectedItemId()) {
                    case R.id.nav_steps:
                        dailySteps();
                        break;
                    case R.id.nav_distance:
                        dailyDistance();
                        break;
                    case R.id.nav_calories:
                        dailyCalories();
                        break;
                }
                break;
            case R.id.nav_week:
                System.out.println("Week");
                barChart.setVisibility(View.VISIBLE);
                //barChart.getXAxis().setAxisMaximum(7);
                lineChart.setVisibility(View.INVISIBLE);
                switch (bottomNavigationViewActivity.getSelectedItemId()) {
                    case R.id.nav_steps:
                        weeklySteps();
                        break;
                    case R.id.nav_distance:
                        weeklyDistance();
                        break;
                    case R.id.nav_calories:
                        weeklyCalories();
                        break;
                }
                break;
            case R.id.nav_month:
                System.out.println("Month");
                barChart.setVisibility(View.VISIBLE);
                //barChart.getXAxis().setAxisMaximum(30);
                lineChart.setVisibility(View.INVISIBLE);
                switch (bottomNavigationViewActivity.getSelectedItemId()) {
                    case R.id.nav_steps:
                        monthlySteps();
                        break;
                    case R.id.nav_distance:
                        monthlyDistance();
                        break;
                    case R.id.nav_calories:
                        monthlyCalories();
                        break;
                }
                break;
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    private boolean onNavigationItemSelectedActivity(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_steps:
                System.out.println("Steps");
                switch (bottomNavigationViewTime.getSelectedItemId()) {
                    case R.id.nav_day:
                        dailySteps();
                        break;
                    case R.id.nav_week:
                        weeklySteps();
                        break;
                    case R.id.nav_month:
                        monthlySteps();
                        break;
                }
                break;
            case R.id.nav_distance:
                System.out.println("Distance");
                switch (bottomNavigationViewTime.getSelectedItemId()) {
                    case R.id.nav_day:
                        dailyDistance();
                        break;
                    case R.id.nav_week:
                        weeklyDistance();
                        break;
                    case R.id.nav_month:
                        monthlyDistance();
                        break;
                }
                break;
            case R.id.nav_calories:
                System.out.println("Calories");
                switch (bottomNavigationViewTime.getSelectedItemId()) {
                    case R.id.nav_day:
                        dailyCalories();
                        break;
                    case R.id.nav_week:
                        weeklyCalories();
                        break;
                    case R.id.nav_month:
                        monthlyCalories();
                        break;
                }
                break;
        }
        return true;
    }

    private void dailySteps() {
        Observer<List<ActivityDetailUtil>> observer = data -> setLineChartData(data, "Steps", 24);
        StepsSnapshotMeasurementRepository stepsSnapshotMeasurementRepository = new StepsSnapshotMeasurementRepository(getContext());
        stepsSnapshotMeasurementRepository.readLastDay(LocalDate.now(), observer);

    }

    private void weeklySteps() {
        Observer<List<ActivityDetailUtil>> observer = data -> setBarChartData(data, "Steps", 7);
        StepsSnapshotMeasurementRepository stepsSnapshotMeasurementRepository = new StepsSnapshotMeasurementRepository(getContext());
        stepsSnapshotMeasurementRepository.readLastSevenDays(LocalDate.now(), observer);
    }

    private void monthlySteps() {
        Observer<List<ActivityDetailUtil>> observer = data -> setBarChartData(data, "Steps", 30);
        StepsSnapshotMeasurementRepository stepsSnapshotMeasurementRepository = new StepsSnapshotMeasurementRepository(getContext());
        stepsSnapshotMeasurementRepository.readLastThirtyDays(LocalDate.now(), observer);
    }

    private void dailyDistance() {
        Observer<List<ActivityDetailUtil>> observer = data -> setLineChartData(data, "Distance", 24);
        DistanceSnapshotMeasurementRepository distanceSnapshotMeasurementRepository = new DistanceSnapshotMeasurementRepository(getContext());
        distanceSnapshotMeasurementRepository.readLastDay(LocalDate.now(), observer);
    }

    private void weeklyDistance() {
        Observer<List<ActivityDetailUtil>> observer = data -> setBarChartData(data, "Distance", 7);
        DistanceSnapshotMeasurementRepository distanceSnapshotMeasurementRepository = new DistanceSnapshotMeasurementRepository(getContext());
        distanceSnapshotMeasurementRepository.readLastSevenDays(LocalDate.now(), observer);
    }

    private void monthlyDistance(){
        Observer<List<ActivityDetailUtil>> observer = data -> setBarChartData(data, "Distance", 30);
        DistanceSnapshotMeasurementRepository distanceSnapshotMeasurementRepository = new DistanceSnapshotMeasurementRepository(getContext());
        distanceSnapshotMeasurementRepository.readLastThirtyDays(LocalDate.now(), observer);
    }

    private void dailyCalories(){
        Observer<List<ActivityDetailUtil>> observer = data -> setLineChartData(data, "Calories", 24);
        CaloriesSnapshotMeasurementRepository caloriesSnapshotMeasurementRepository = new CaloriesSnapshotMeasurementRepository(getContext());
        caloriesSnapshotMeasurementRepository.readLastDay(LocalDate.now(), observer);
    }

    private void weeklyCalories(){
        Observer<List<ActivityDetailUtil>> observer = data -> setBarChartData(data, "Calories", 7);
        CaloriesSnapshotMeasurementRepository caloriesSnapshotMeasurementRepository = new CaloriesSnapshotMeasurementRepository(getContext());
        caloriesSnapshotMeasurementRepository.readLastSevenDays(LocalDate.now(), observer);
    }

    private void monthlyCalories(){
        Observer<List<ActivityDetailUtil>> observer = data -> setBarChartData(data, "Calories", 30);
        CaloriesSnapshotMeasurementRepository caloriesSnapshotMeasurementRepository = new CaloriesSnapshotMeasurementRepository(getContext());
        caloriesSnapshotMeasurementRepository.readLastThirtyDays(LocalDate.now(), observer);
    }

    private void setBarChartData(List<ActivityDetailUtil> list, String label, int max) {
        int currentTime = 0;
        List<BarEntry> entries = new ArrayList<>();

        for (ActivityDetailUtil data : list) {
            if (currentTime != data.getTime()) {
                while (currentTime != data.getTime()) {
                    currentTime++;
                    entries.add(new BarEntry(currentTime, 0));
                }
            }
            entries.add(new BarEntry(data.getTime() + 1, data.getValue()));
            currentTime++;
        }

        if(currentTime != max){
            while(currentTime != max){
                entries.add(new BarEntry(currentTime, 0));
                currentTime++;
            }
        }

        BarDataSet barDataSet = new BarDataSet(entries, label);
        ArrayList<IBarDataSet> dataSet = new ArrayList<>();
        dataSet.add(barDataSet);
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getDescription().setText(label);
        barChart.notifyDataSetChanged();
        barChart.invalidate();
    }

    private void setLineChartData(List<ActivityDetailUtil> list, String label, int max) {
        List<Entry> entries = new ArrayList<>();
        int currentTime = 1;

        for (ActivityDetailUtil data : list) {
            //System.out.println(data.getValue() + " " + data.getTime());
            if (currentTime != data.getTime()) {
                while (currentTime != data.getTime()) {
                    currentTime++;
                    entries.add(new BarEntry(currentTime, 0));
                }
            }
            entries.add(new BarEntry(data.getTime() + 1, data.getValue()));
            currentTime++;
        }

        if (currentTime != max) {
            while (currentTime != max) {
                currentTime++;
                entries.add(new BarEntry(currentTime, 0));
            }
        }

        LineDataSet lineDataSet = new LineDataSet(entries, label);
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        lineDataSet.setDrawFilled(true);
        ArrayList<ILineDataSet> dataSet = new ArrayList<>();
        dataSet.add(lineDataSet);
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setText(label);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

}
