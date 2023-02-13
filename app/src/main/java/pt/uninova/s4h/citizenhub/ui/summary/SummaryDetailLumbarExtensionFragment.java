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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(SummaryViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary_detail_lumbar_extension, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
