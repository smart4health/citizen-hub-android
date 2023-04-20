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
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyHeartRatePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyHeartRatePanel;
import pt.uninova.s4h.citizenhub.persistence.repository.HeartRateMeasurementRepository;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** This class aims to manage the detailed posture layout */
public class SummaryDetailHeartRateFragment extends Fragment {

    private SummaryViewModel model;
    private ChartFunctions chartFunctions;
    private LineChart lineChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(SummaryViewModel.class);
        chartFunctions = new ChartFunctions(getContext(), LocalDate.now());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary_detail_heart_rate, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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
                    dailyHeartRate();
                } else if(pos == 1) {
                    textViewXLabel.setText(getString(R.string.summary_detail_time_days));
                    lineChart.highlightValue(null);
                    lineChart.getXAxis().resetAxisMaximum();
                    weeklyHeartRate();
                } else if(pos == 2) {
                    textViewXLabel.setText(getString(R.string.summary_detail_time_days));
                    lineChart.highlightValue(null);
                    lineChart.getXAxis().resetAxisMaximum();
                    monthlyHeartRate();
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
        dailyHeartRate();
    }

    /** Calls a query to retrieve daily heart rate and adds the information retrieved to the chart.
     * @return
     */
    private void dailyHeartRate(){
        Observer<List<HourlyHeartRatePanel>> observer = heartRate -> chartFunctions.setLineChartData(lineChart, chartFunctions.parseHeartRateUtil(heartRate),new String[]{getString(R.string.summary_detail_heart_rate_average), getString(R.string.summary_detail_heart_rate_maximum), getString(R.string.summary_detail_heart_rate_minimum)}, 24);
        HeartRateMeasurementRepository heartRateMeasurementRepository = new HeartRateMeasurementRepository(getContext());
        heartRateMeasurementRepository.selectLastDay(LocalDate.now(), observer);
    }

    /** Calls a query to retrieve weekly heart rate and adds the information retrieved to the chart.
     * @return
     */
    private void weeklyHeartRate(){
        Observer<List<DailyHeartRatePanel>> observer = heartRate -> chartFunctions.setLineChartData(lineChart, chartFunctions.parseHeartRateUtil(heartRate, 7), new String[]{getString(R.string.summary_detail_heart_rate_average), getString(R.string.summary_detail_heart_rate_maximum), getString(R.string.summary_detail_heart_rate_minimum)}, 7);
        HeartRateMeasurementRepository heartRateMeasurementRepository = new HeartRateMeasurementRepository(getContext());
        heartRateMeasurementRepository.selectSeveralDays(LocalDate.now(), 7, observer);
    }

    /** Calls a query to retrieve monthly heart rate and adds the information retrieved to the chart.
     * @return
     */
    private void monthlyHeartRate(){
        Observer<List<DailyHeartRatePanel>> observer = heartRate -> chartFunctions.setLineChartData(lineChart, chartFunctions.parseHeartRateUtil(heartRate, 30), new String[]{getString(R.string.summary_detail_heart_rate_average), getString(R.string.summary_detail_heart_rate_maximum), getString(R.string.summary_detail_heart_rate_minimum)}, 30);
        HeartRateMeasurementRepository heartRateMeasurementRepository = new HeartRateMeasurementRepository(getContext());
        heartRateMeasurementRepository.selectSeveralDays(LocalDate.now(), 30, observer);
    }

}
