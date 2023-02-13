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
import com.github.mikephil.charting.charts.PieChart;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.persistence.entity.util.DailyPosturePanel;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyPosturePanel;
import pt.uninova.s4h.citizenhub.persistence.repository.PostureMeasurementRepository;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** This class aims to manage the detailed posture layout */
public class SummaryDetailPostureFragment extends Fragment {

    private SummaryViewModel model;
    private ChartFunctions chartFunctions;
    private LineChart lineChart;
    private PieChart pieChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(SummaryViewModel.class);
        chartFunctions = new ChartFunctions(getContext(), LocalDate.now());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary_detail_posture, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lineChart = view.findViewById(R.id.line_chart);
        pieChart = view.findViewById(R.id.pie_chart);

        TextView textViewXLabel = view.findViewById(R.id.text_view_x_axis_label);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();

                if(pos == 0) {
                    textViewXLabel.setText(getString(R.string.summary_detail_time_hours));
                    lineChart.highlightValue(null);
                    lineChart.getXAxis().setAxisMaximum(24);
                    pieChart.setVisibility(View.VISIBLE);
                    dailyPosture();
                } else if(pos == 1) {
                    textViewXLabel.setText(getString(R.string.summary_detail_time_days));
                    lineChart.highlightValue(null);
                    lineChart.getXAxis().resetAxisMaximum();
                    pieChart.setVisibility(View.INVISIBLE);
                    weeklyPosture();
                } else if(pos == 2) {
                    textViewXLabel.setText(getString(R.string.summary_detail_time_days));
                    lineChart.highlightValue(null);
                    lineChart.getXAxis().resetAxisMaximum();
                    pieChart.setVisibility(View.INVISIBLE);
                    monthlyPosture();
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
        chartFunctions.setupPieChart(pieChart);
        // Specific to this fragment
        lineChart.getAxisLeft().setAxisMaximum(100);

        dailyPosture();
    }
    /** Calls a query to retrieve daily posture info and adds the information retrieved to the chart.
     * @return
     */
    private void dailyPosture() {
        PostureMeasurementRepository postureMeasurementRepository = new PostureMeasurementRepository(getContext());
        Observer<List<HourlyPosturePanel>> observer = posture -> {
            chartFunctions.setAreaChart(lineChart, chartFunctions.parsePostureUtil(posture), new String[]{getString(R.string.summary_detail_posture_correct), getString(R.string.summary_detail_posture_incorrect)}, 24);
            chartFunctions.setPieChart(pieChart, chartFunctions.parsePostureUtil(posture));
        };
        postureMeasurementRepository.readLastDayPosture(LocalDate.now(), observer);
    }

    /** Calls a query to retrieve weekly posture info and adds the information retrieved to the chart.
     * @return
     */
    private void weeklyPosture() {
        PostureMeasurementRepository postureMeasurementRepository = new PostureMeasurementRepository(getContext());
        Observer<List<DailyPosturePanel>> observer = posture -> {
            chartFunctions.setAreaChart(lineChart, chartFunctions.parsePostureUtil(posture, 7), new String[]{getString(R.string.summary_detail_posture_correct), getString(R.string.summary_detail_posture_incorrect)}, 7);
        };
        postureMeasurementRepository.readSeveralDaysPosture(LocalDate.now(), 7, observer);
    }

    /** Calls a query to retrieve monthly posture info and adds the information retrieved to the chart.
     * @return
     */
    private void monthlyPosture() {
        PostureMeasurementRepository postureMeasurementRepository = new PostureMeasurementRepository(getContext());
        Observer<List<DailyPosturePanel>> observer = posture -> {
            chartFunctions.setAreaChart(lineChart, chartFunctions.parsePostureUtil(posture, 30), new String[]{getString(R.string.summary_detail_posture_correct), getString(R.string.summary_detail_posture_incorrect)}, 30);
        };
        postureMeasurementRepository.readSeveralDaysPosture(LocalDate.now(), 30, observer);
    }

}
