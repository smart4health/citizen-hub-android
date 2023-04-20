package pt.uninova.s4h.citizenhub.ui.summary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyBloodPressurePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyBloodPressurePanel;
import pt.uninova.s4h.citizenhub.persistence.repository.BloodPressureMeasurementRepository;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** This class aims to manage the detailed blood pressure layout */
public class SummaryDetailBloodPressureFragment extends Fragment {

    private SummaryViewModel model;
    private LineChart lineChart;
    private ChartFunctions chartFunctions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(SummaryViewModel.class);
        chartFunctions = new ChartFunctions(getContext(), LocalDate.now());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary_detail_blood_pressure, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        lineChart = view.findViewById(R.id.line_chart);

        TextView textViewXLabel = view.findViewById(R.id.text_view_x_axis_label);

        TabLayout tabLayout = requireView().findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();

                if(pos == 0) {
                    textViewXLabel.setText(getString(R.string.summary_detail_time_hours));
                    lineChart.highlightValue(null);
                    lineChart.getXAxis().setAxisMaximum(24);
                    dailyBloodPressure();
                } else if(pos == 1) {
                    textViewXLabel.setText(getString(R.string.summary_detail_time_days));
                    lineChart.highlightValue(null);
                    lineChart.getXAxis().resetAxisMaximum();
                    weeklyBloodPressure();
                } else if(pos == 2) {
                    textViewXLabel.setText(getString(R.string.summary_detail_time_days));
                    lineChart.highlightValue(null);
                    lineChart.getXAxis().resetAxisMaximum();
                    monthlyBloodPressure();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        chartFunctions.setupLineChart(lineChart, model.getChartViewMarker());
        dailyBloodPressure();
    }

    /** Calls a query to retrieve daily blood pressure and adds the information retrieved to the chart.
     * @return
     */
    private void dailyBloodPressure() {
        Observer<List<HourlyBloodPressurePanel>> observer = bloodPressure -> chartFunctions.setLineChartData(lineChart, chartFunctions.parseBloodPressureUtil(bloodPressure), new String[]{getString(R.string.summary_detail_blood_pressure_systolic), getString(R.string.summary_detail_blood_pressure_diastolic), getString(R.string.summary_detail_blood_pressure_mean)}, 24);
        BloodPressureMeasurementRepository bloodPressureMeasurementRepository = new BloodPressureMeasurementRepository(getContext());
        bloodPressureMeasurementRepository.readLastDay(LocalDate.now(), observer);
    }

    /** Calls a query to retrieve weekly blood pressure and adds the information retrieved to the chart.
     * @return
     */
    private void weeklyBloodPressure() {
        Observer<List<DailyBloodPressurePanel>> observer = bloodPressure -> chartFunctions.setLineChartData(lineChart, chartFunctions.parseBloodPressureUtil(bloodPressure, 7), new String[]{getString(R.string.summary_detail_blood_pressure_systolic), getString(R.string.summary_detail_blood_pressure_diastolic), getString(R.string.summary_detail_blood_pressure_mean)}, 7);
        BloodPressureMeasurementRepository bloodPressureMeasurementRepository = new BloodPressureMeasurementRepository(getContext());
        bloodPressureMeasurementRepository.readSeveralDays(LocalDate.now(), 7, observer);
    }

    /** Calls a query to retrieve monthly blood pressure and adds the information retrieved to the chart.
     * @return
     */
    private void monthlyBloodPressure() {
        Observer<List<DailyBloodPressurePanel>> observer = bloodPressure -> chartFunctions.setLineChartData(lineChart, chartFunctions.parseBloodPressureUtil(bloodPressure, 30), new String[]{getString(R.string.summary_detail_blood_pressure_systolic), getString(R.string.summary_detail_blood_pressure_diastolic), getString(R.string.summary_detail_blood_pressure_mean)}, 30);
        BloodPressureMeasurementRepository bloodPressureMeasurementRepository = new BloodPressureMeasurementRepository(getContext());
        bloodPressureMeasurementRepository.readSeveralDays(LocalDate.now(), 30, observer);
    }

}
