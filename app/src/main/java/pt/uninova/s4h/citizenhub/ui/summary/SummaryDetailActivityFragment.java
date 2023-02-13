package pt.uninova.s4h.citizenhub.ui.summary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyCaloriesPanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyDistancePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyStepsPanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyCaloriesPanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyDistancePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyStepsPanel;
import pt.uninova.s4h.citizenhub.persistence.repository.CaloriesMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.DistanceMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.StepsMeasurementRepository;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class SummaryDetailActivityFragment extends Fragment {

    private SummaryViewModel model;
    private ChartFunctions chartFunctions;
    private BarChart barChart;
    private TabLayout tabLayout;
    private TabLayout tabLayoutActivity;
    private TextView textViewLabel;
    private TextView textViewXLabel;
    private TextView textViewYLabel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(SummaryViewModel.class);
        chartFunctions = new ChartFunctions(getContext(), LocalDate.now());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary_detail_activity, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barChart = view.findViewById(R.id.bar_chart);

        textViewLabel = view.findViewById(R.id.tv_activity);
        textViewXLabel = view.findViewById(R.id.text_view_x_axis_label);
        textViewYLabel = view.findViewById(R.id.text_view_y_axis_label);

        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayoutActivity = view.findViewById(R.id.tab_layout_activity);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();

                if(pos == 0) {
                    textViewXLabel.setText(getString(R.string.summary_detail_time_hours));
                    barChart.highlightValue(null);
                    switch(tabLayoutActivity.getSelectedTabPosition()) {
                        case 0: dailySteps(); break;
                        case 1: dailyDistance(); break;
                        case 2: dailyCalories(); break;
                    }
                } else if(pos == 1) {
                    textViewXLabel.setText(getString(R.string.summary_detail_time_days));
                    barChart.highlightValue(null);
                    switch(tabLayoutActivity.getSelectedTabPosition()) {
                        case 0: weeklySteps(); break;
                        case 1: weeklyDistance(); break;
                        case 2: weeklyCalories(); break;
                    }
                } else if(pos == 2) {
                    textViewXLabel.setText(getString(R.string.summary_detail_time_days));
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
                    textViewLabel.setText(getString(R.string.summary_detail_activity_steps));
                    textViewYLabel.setText(getString(R.string.summary_detail_activity_steps));
                    barChart.highlightValue(null);
                    switch(tabLayout.getSelectedTabPosition()) {
                        case 0: dailySteps(); break;
                        case 1: weeklySteps(); break;
                        case 2: monthlySteps(); break;
                    }
                } else if(pos == 1) {
                    textViewLabel.setText(getString(R.string.summary_detail_activity_distance));
                    textViewYLabel.setText(getString(R.string.summary_detail_activity_distance_with_units));
                    barChart.highlightValue(null);
                    switch(tabLayout.getSelectedTabPosition()) {
                        case 0: dailyDistance(); break;
                        case 1: weeklyDistance(); break;
                        case 2: monthlyDistance(); break;
                    }
                } else if(pos == 2) {
                    textViewLabel.setText(getString(R.string.summary_detail_activity_calories));
                    textViewYLabel.setText(getString(R.string.summary_detail_activity_calories_with_units));
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
        Observer<List<HourlyStepsPanel>> observer = steps -> chartFunctions.setBarChartData(barChart, chartFunctions.parseStepsUtil(steps), getString(R.string.summary_detail_activity_steps), 24);
        StepsMeasurementRepository stepsMeasurementRepository =  new StepsMeasurementRepository((getContext()));
        stepsMeasurementRepository.readLastDay(LocalDate.now(), observer);
    }

    private void weeklySteps() {
        Observer<List<DailyStepsPanel>> observer = steps -> chartFunctions.setBarChartData(barChart, chartFunctions.parseStepsUtil(steps, 7), getString(R.string.summary_detail_activity_steps), 7);
        StepsMeasurementRepository stepsMeasurementRepository =  new StepsMeasurementRepository((getContext()));
        stepsMeasurementRepository.readSeveralDays(LocalDate.now(), 7, observer);
    }

    private void monthlySteps() {
        Observer<List<DailyStepsPanel>> observer = steps -> chartFunctions.setBarChartData(barChart, chartFunctions.parseStepsUtil(steps, 30), getString(R.string.summary_detail_activity_steps), 30);
        StepsMeasurementRepository stepsMeasurementRepository =  new StepsMeasurementRepository((getContext()));
        stepsMeasurementRepository.readSeveralDays(LocalDate.now(), 30, observer);
        //StepsSnapshotMeasurementRepository stepsSnapshotMeasurementRepository = new StepsSnapshotMeasurementRepository(getContext());
        //stepsSnapshotMeasurementRepository.readLastThirtyDays(LocalDate.now(), observer);
    }

    private void dailyDistance() {
        Observer<List<HourlyDistancePanel>> observer = distance -> chartFunctions.setBarChartData(barChart, chartFunctions.parseDistanceUtil(distance), getString(R.string.summary_detail_activity_distance), 24);
        DistanceMeasurementRepository distanceMeasurementRepository = new DistanceMeasurementRepository(getContext());
        distanceMeasurementRepository.readLastDay(LocalDate.now(), observer);
    }

    private void weeklyDistance() {
        Observer<List<DailyDistancePanel>> observer = distance -> chartFunctions.setBarChartData(barChart, chartFunctions.parseDistanceUtil(distance, 7), getString(R.string.summary_detail_activity_distance), 7);
        DistanceMeasurementRepository distanceMeasurementRepository = new DistanceMeasurementRepository(getContext());
        distanceMeasurementRepository.readSeveralDays(LocalDate.now(), 7, observer);
    }

    private void monthlyDistance(){
        Observer<List<DailyDistancePanel>> observer = distance -> chartFunctions.setBarChartData(barChart, chartFunctions.parseDistanceUtil(distance, 30), getString(R.string.summary_detail_activity_distance), 30);
        DistanceMeasurementRepository distanceMeasurementRepository = new DistanceMeasurementRepository(getContext());
        distanceMeasurementRepository.readSeveralDays(LocalDate.now(), 30, observer);
    }

    private void dailyCalories(){
        Observer<List<HourlyCaloriesPanel>> observer = calories -> chartFunctions.setBarChartData(barChart, chartFunctions.parseCaloriesUtil(calories), getString(R.string.summary_detail_activity_calories), 24);
        CaloriesMeasurementRepository caloriesMeasurementRepository = new CaloriesMeasurementRepository(getContext());
        caloriesMeasurementRepository.readLastDay(LocalDate.now(), observer);
    }

    private void weeklyCalories(){
        Observer<List<DailyCaloriesPanel>> observer = calories -> chartFunctions.setBarChartData(barChart, chartFunctions.parseCaloriesUtil(calories, 7), getString(R.string.summary_detail_activity_calories), 7);
        CaloriesMeasurementRepository caloriesMeasurementRepository = new CaloriesMeasurementRepository(getContext());
        caloriesMeasurementRepository.readSeveralDays(LocalDate.now(), 7, observer);
    }

    private void monthlyCalories(){
        Observer<List<DailyCaloriesPanel>> observer = calories -> chartFunctions.setBarChartData(barChart, chartFunctions.parseCaloriesUtil(calories, 30), getString(R.string.summary_detail_activity_calories), 30);
        CaloriesMeasurementRepository caloriesMeasurementRepository = new CaloriesMeasurementRepository(getContext());
        caloriesMeasurementRepository.readSeveralDays(LocalDate.now(), 30, observer);
    }

}
