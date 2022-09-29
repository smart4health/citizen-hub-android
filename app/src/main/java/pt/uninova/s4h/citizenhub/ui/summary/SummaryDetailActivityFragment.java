package pt.uninova.s4h.citizenhub.ui.summary;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailUtil;
import pt.uninova.s4h.citizenhub.persistence.repository.CaloriesSnapshotMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.DistanceSnapshotMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.StepsSnapshotMeasurementRepository;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class SummaryDetailActivityFragment extends Fragment {

    private SummaryViewModel model;
    private ChartFunctions chartFunctions;
    private BarChart barChart;
    private TabLayout tabLayout;
    private TabLayout tabLayoutActivity;
    private TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(SummaryViewModel.class);
        chartFunctions = new ChartFunctions(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary_detail_activity, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barChart = requireView().findViewById(R.id.bar_chart);

        textView = requireView().findViewById(R.id.tv_activity);

        tabLayout = requireView().findViewById(R.id.tab_layout);
        tabLayoutActivity = requireView().findViewById(R.id.tab_layout_activity);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();

                if(pos == 0) {
                    barChart.highlightValue(null);
                    switch(tabLayoutActivity.getSelectedTabPosition()) {
                        case 0: dailySteps(); break;
                        case 1: dailyDistance(); break;
                        case 2: dailyCalories(); break;
                    }
                } else if(pos == 1) {
                    barChart.highlightValue(null);
                    switch(tabLayoutActivity.getSelectedTabPosition()) {
                        case 0: weeklySteps(); break;
                        case 1: weeklyDistance(); break;
                        case 2: weeklyCalories(); break;
                    }
                } else if(pos == 2) {
                    barChart.highlightValue(null);
                    switch(tabLayoutActivity.getSelectedTabPosition()) {
                        case 0: monthlySteps(); break;
                        case 1: monthlyDistance(); break;
                        case 2: monthlyCalories(); break;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tabLayoutActivity.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();

                if(pos == 0) {
                    textView.setText(getString(R.string.summary_detail_activity_steps));
                    barChart.highlightValue(null);
                    switch(tabLayout.getSelectedTabPosition()) {
                        case 0: dailySteps(); break;
                        case 1: weeklySteps(); break;
                        case 2: monthlySteps(); break;
                    }
                } else if(pos == 1) {
                    textView.setText(getString(R.string.summary_detail_activity_distance));
                    barChart.highlightValue(null);
                    switch(tabLayout.getSelectedTabPosition()) {
                        case 0: dailyDistance(); break;
                        case 1: weeklyDistance(); break;
                        case 2: monthlyDistance(); break;
                    }
                } else if(pos == 2) {
                    textView.setText(getString(R.string.summary_detail_activity_calories));
                    barChart.highlightValue(null);
                    switch(tabLayout.getSelectedTabPosition()) {
                        case 0: dailyCalories(); break;
                        case 1: weeklyCalories(); break;
                        case 2: monthlyCalories(); break;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        chartFunctions.setupBarChart(barChart, model.getChartViewMarker());
        dailySteps();
    }

    private void dailySteps() {
        Observer<List<SummaryDetailUtil>> observer = data -> chartFunctions.setBarChartData(data, barChart, getString(R.string.summary_detail_activity_steps), 24);
        StepsSnapshotMeasurementRepository stepsSnapshotMeasurementRepository = new StepsSnapshotMeasurementRepository(getContext());
        stepsSnapshotMeasurementRepository.readLastDay(LocalDate.now(), observer);
    }

    private void weeklySteps() {
        Observer<List<SummaryDetailUtil>> observer = data -> chartFunctions.setBarChartData(data, barChart, getString(R.string.summary_detail_activity_steps), 7);
        StepsSnapshotMeasurementRepository stepsSnapshotMeasurementRepository = new StepsSnapshotMeasurementRepository(getContext());
        stepsSnapshotMeasurementRepository.readLastSevenDays(LocalDate.now(), observer);
    }

    private void monthlySteps() {
        Observer<List<SummaryDetailUtil>> observer = data -> chartFunctions.setBarChartData(data, barChart, getString(R.string.summary_detail_activity_steps), 30);
        StepsSnapshotMeasurementRepository stepsSnapshotMeasurementRepository = new StepsSnapshotMeasurementRepository(getContext());
        stepsSnapshotMeasurementRepository.readLastThirtyDays(LocalDate.now(), observer);
    }

    private void dailyDistance() {
        Observer<List<SummaryDetailUtil>> observer = data -> chartFunctions.setBarChartData(data, barChart, getString(R.string.summary_detail_activity_distance), 24);
        DistanceSnapshotMeasurementRepository distanceSnapshotMeasurementRepository = new DistanceSnapshotMeasurementRepository(getContext());
        distanceSnapshotMeasurementRepository.readLastDay(LocalDate.now(), observer);
    }

    private void weeklyDistance() {
        Observer<List<SummaryDetailUtil>> observer = data -> chartFunctions.setBarChartData(data, barChart, getString(R.string.summary_detail_activity_distance), 7);
        DistanceSnapshotMeasurementRepository distanceSnapshotMeasurementRepository = new DistanceSnapshotMeasurementRepository(getContext());
        distanceSnapshotMeasurementRepository.readLastSevenDays(LocalDate.now(), observer);
    }

    private void monthlyDistance(){
        Observer<List<SummaryDetailUtil>> observer = data -> chartFunctions.setBarChartData(data, barChart, getString(R.string.summary_detail_activity_distance), 30);
        DistanceSnapshotMeasurementRepository distanceSnapshotMeasurementRepository = new DistanceSnapshotMeasurementRepository(getContext());
        distanceSnapshotMeasurementRepository.readLastThirtyDays(LocalDate.now(), observer);
    }

    private void dailyCalories(){
        Observer<List<SummaryDetailUtil>> observer = data -> chartFunctions.setBarChartData(data, barChart, getString(R.string.summary_detail_activity_calories), 24);
        CaloriesSnapshotMeasurementRepository caloriesSnapshotMeasurementRepository = new CaloriesSnapshotMeasurementRepository(getContext());
        caloriesSnapshotMeasurementRepository.readLastDay(LocalDate.now(), observer);
    }

    private void weeklyCalories(){
        Observer<List<SummaryDetailUtil>> observer = data -> chartFunctions.setBarChartData(data, barChart, getString(R.string.summary_detail_activity_calories), 7);
        CaloriesSnapshotMeasurementRepository caloriesSnapshotMeasurementRepository = new CaloriesSnapshotMeasurementRepository(getContext());
        caloriesSnapshotMeasurementRepository.readLastSevenDays(LocalDate.now(), observer);
    }

    private void monthlyCalories(){
        Observer<List<SummaryDetailUtil>> observer = data -> chartFunctions.setBarChartData(data, barChart, getString(R.string.summary_detail_activity_calories), 30);
        CaloriesSnapshotMeasurementRepository caloriesSnapshotMeasurementRepository = new CaloriesSnapshotMeasurementRepository(getContext());
        caloriesSnapshotMeasurementRepository.readLastThirtyDays(LocalDate.now(), observer);
    }

}
