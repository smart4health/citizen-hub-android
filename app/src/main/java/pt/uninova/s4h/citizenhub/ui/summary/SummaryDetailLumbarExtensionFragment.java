package pt.uninova.s4h.citizenhub.ui.summary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.persistence.entity.util.LumbarExtensionWithTimestampPanel;
import pt.uninova.s4h.citizenhub.persistence.repository.LumbarExtensionTrainingRepository;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** This class aims to manage the detailed lumbar extension training layout */
public class SummaryDetailLumbarExtensionFragment extends Fragment {

    private SummaryViewModel model;

    private TableLayout tableLayout;
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

        /*lineChart = requireView().findViewById(R.id.line_chart);

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
                    //getDuration();
                } else if(pos == 1) {
                    System.out.println("Score");
                    lineChart.highlightValue(null);
                    textView.setText(getString(R.string.summary_detail_lumbar_extension_score));
                    //getScore();
                } else if(pos == 2) {
                    System.out.println("Repetitions");
                    lineChart.highlightValue(null);
                    textView.setText(getString(R.string.summary_detail_lumbar_extension_repetitions));
                    //getRepetitions();
                } else if(pos == 3) {
                    System.out.println("Weight");
                    lineChart.highlightValue(null);
                    textView.setText(getString(R.string.summary_detail_lumbar_extension_weight));
                    //getWeight();
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
        getRepetitions();*/
        tableLayout = view.findViewById(R.id.lumbarExtensionTableLayout);
        fillFragment();
    }

    /** Calls a query to retrieve daily lumbar extension trainings and draws the information on the fragment layout.
     * @return
     */
    private void fillFragment(){
        Observer<List<LumbarExtensionWithTimestampPanel>> observer = data -> {
            for(LumbarExtensionWithTimestampPanel lumbarExtensionWithTimestampPanel : data){
                requireActivity().runOnUiThread(() -> {
                    View vTimestamp = LayoutInflater.from(getContext()).inflate(R.layout.fragment_report_timestamp, null);
                    TextView tvTimestamp = vTimestamp.findViewById(R.id.tvTimestamp);
                    tvTimestamp.setPadding(0, 15, 0, 0);
                    tvTimestamp.setText(lumbarExtensionWithTimestampPanel.getTimestamp().substring(lumbarExtensionWithTimestampPanel.getTimestamp().indexOf("T") + 1, lumbarExtensionWithTimestampPanel.getTimestamp().indexOf("Z")));
                    tableLayout.addView(vTimestamp);
                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
                    fillRow(requireContext().getString(R.string.report_lumbar_training_duration_label), secondsToString((long) lumbarExtensionWithTimestampPanel.getDuration().floatValue() / 1000), null);
                    fillRow(requireContext().getString(R.string.report_lumbar_training_repetitions_label), decimalFormat.format(lumbarExtensionWithTimestampPanel.getRepetitions()), null);
                    fillRow(requireContext().getString(R.string.report_lumbar_training_score_label), decimalFormat.format(lumbarExtensionWithTimestampPanel.getScore()), requireContext().getString(R.string.report_lumbar_training_score_units));
                    fillRow(requireContext().getString(R.string.report_lumbar_training_weight_label), decimalFormat.format(lumbarExtensionWithTimestampPanel.getWeight()), requireContext().getString(R.string.report_lumbar_training_weight_units));
                });
            }
        };
        LumbarExtensionTrainingRepository lumbarExtensionTrainingRepository = new LumbarExtensionTrainingRepository(getContext());
        lumbarExtensionTrainingRepository.selectTrainingSection(LocalDate.now(), observer);
    }

    /*private void getDuration(){
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
    }*/

    /** Draws a row on the TabLayout of the fragment.
     * @return
     */
    private void fillRow(String label, String value, String units){
        View v = LayoutInflater.from(getContext()).inflate(R.layout.fragment_report_rows, null);
        TextView tvLabel = v.findViewById(R.id.tvLabel);
        TextView tvValue = v.findViewById(R.id.tvValueMyTime);
        TextView tvUnits = v.findViewById(R.id.tvUnitsMyTime);
        TextView tvValueWorkTime = v.findViewById(R.id.tvValueWorkTime);
        TextView tvUnitsWorkTime = v.findViewById(R.id.tvUnitsWorkTime);

        tvLabel.setText(label);
        tvValue.setText(value);
        tvUnits.setText(units);

        tvValueWorkTime.setVisibility(View.INVISIBLE);
        tvUnitsWorkTime.setVisibility(View.INVISIBLE);
        tvUnitsWorkTime.setText(units);

        tableLayout.addView(v);
    }

    /*private void setLineChartData(List<SummaryDetailUtil> list, String label){
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
    }*/

    /** Converts a long value representing a time in seconds to a string containing hours, minutes and seconds.
     * @param value Value to convert to string.
     * @return A string with the converted value.
     */
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
