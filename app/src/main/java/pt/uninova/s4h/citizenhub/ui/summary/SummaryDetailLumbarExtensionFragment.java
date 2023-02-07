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
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailUtil;
import pt.uninova.s4h.citizenhub.persistence.repository.LumbarExtensionTrainingRepository;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class SummaryDetailLumbarExtensionFragment extends Fragment {

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
        return inflater.inflate(R.layout.fragment_summary_detail_lumbar_extension, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lineChart = requireView().findViewById(R.id.line_chart);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        TextView textView = view.findViewById(R.id.text_view);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();

                if(pos == 0) {
                    System.out.println("Duration");
                    lineChart.highlightValue(null);
                    textView.setText(getString(R.string.summary_detail_lumbar_extension_duration));
                    getDuration();
                } else if(pos == 1) {
                    System.out.println("Score");
                    lineChart.highlightValue(null);
                    textView.setText(getString(R.string.summary_detail_lumbar_extension_score));
                    getScore();
                } else if(pos == 2) {
                    System.out.println("Repetitions");
                    lineChart.highlightValue(null);
                    textView.setText(getString(R.string.summary_detail_lumbar_extension_repetitions));
                    getRepetitions();
                } else if(pos == 3) {
                    System.out.println("Weight");
                    lineChart.highlightValue(null);
                    textView.setText(getString(R.string.summary_detail_lumbar_extension_weight));
                    getWeight();
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
        lineChart.getXAxis().resetAxisMaximum();
        getRepetitions();
    }

    private void getDuration(){
        Observer<List<SummaryDetailUtil>> observer = data -> setLineChartData(data, getString(R.string.summary_detail_lumbar_extension_duration));
        LumbarExtensionTrainingRepository lumbarExtensionTrainingRepository = new LumbarExtensionTrainingRepository(getContext());
        lumbarExtensionTrainingRepository.selectDuration(observer);
    }

    private void getScore(){
        Observer<List<SummaryDetailUtil>> observer = data -> setLineChartData(data, getString(R.string.summary_detail_lumbar_extension_score));
        LumbarExtensionTrainingRepository lumbarExtensionTrainingRepository = new LumbarExtensionTrainingRepository(getContext());
        lumbarExtensionTrainingRepository.selectScore(observer);
    }

    private void getRepetitions(){
        Observer<List<SummaryDetailUtil>> observer = data -> setLineChartData(data, getString(R.string.summary_detail_lumbar_extension_repetitions));
        LumbarExtensionTrainingRepository lumbarExtensionTrainingRepository = new LumbarExtensionTrainingRepository(getContext());
        lumbarExtensionTrainingRepository.selectRepetitions(observer);
    }

    private void getWeight(){
        Observer<List<SummaryDetailUtil>> observer = data -> setLineChartData(data, getString(R.string.summary_detail_lumbar_extension_weight));
        LumbarExtensionTrainingRepository lumbarExtensionTrainingRepository = new LumbarExtensionTrainingRepository(getContext());
        lumbarExtensionTrainingRepository.selectWeight(observer);
    }

    private void setLineChartData(List<SummaryDetailUtil> list, String label){
        List<Entry> entries = new ArrayList<>();
        int x = 0;
        if(list.size() == 1)
            x++;
        for(SummaryDetailUtil data : list){
            entries.add(new BarEntry(x++, data.getValue1()));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, label);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setColor(requireContext().getColor(R.color.colorS4HLightBlue));
        lineDataSet.setCircleColor(requireContext().getColor(R.color.colorS4HLightBlue));
        lineDataSet.setCircleHoleColor(requireContext().getColor(R.color.colorS4HLightBlue));

        ArrayList<ILineDataSet> dataSet = new ArrayList<>();
        dataSet.add(lineDataSet);

        LineData lineData = new LineData(dataSet);
        lineData.setValueFormatter(new ChartValueFormatter());
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

}
