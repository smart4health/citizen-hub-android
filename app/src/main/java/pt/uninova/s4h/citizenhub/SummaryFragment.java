package pt.uninova.s4h.citizenhub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Map;

import pt.uninova.s4h.citizenhub.persistence.MeasurementAggregate;
import pt.uninova.s4h.citizenhub.persistence.MeasurementKind;

public class SummaryFragment extends Fragment {

    private SummaryViewModel model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        model = new ViewModelProvider(requireActivity()).get(SummaryViewModel.class);

        model.getDailySummary().observe(getViewLifecycleOwner(), this::onDailySummaryUpdate);
    }

    private void onDailySummaryUpdate(Map<MeasurementKind, MeasurementAggregate> dailySummary) {
        final LinearLayout caloriesGroup = requireView().findViewById(R.id.calories_group);
        final LinearLayout distanceGroup = requireView().findViewById(R.id.distance_group);
        final LinearLayout heartrateGroup = requireView().findViewById(R.id.heartrate_group);
        final LinearLayout postureGroup = requireView().findViewById(R.id.sitting_group);
        final LinearLayout stepsGroup = requireView().findViewById(R.id.steps_group);
        final LinearLayout noDataGroup = requireView().findViewById(R.id.no_data_group);

        final TextView caloriesTextView = requireView().findViewById(R.id.fragment_summary_text_view_calories);
        final TextView distanceTextView = requireView().findViewById(R.id.fragment_summary_text_view_distance);
        final TextView heartRateTextView = requireView().findViewById(R.id.fragment_summary_text_view_heart_rate);
        final TextView postureTextView = requireView().findViewById(R.id.fragment_summary_text_view_posture);
        final TextView stepsTextView = requireView().findViewById(R.id.fragment_summary_text_view_steps);
        final TextView noDataTextView = requireView().findViewById(R.id.fragment_summary_text_view_no_data);

        if (dailySummary != null) {
            final MeasurementAggregate calories = dailySummary.get(MeasurementKind.CALORIES);
            final MeasurementAggregate distance = dailySummary.get(MeasurementKind.DISTANCE);
            final MeasurementAggregate heartRate = dailySummary.get(MeasurementKind.HEART_RATE);
            final MeasurementAggregate badPosture = dailySummary.get(MeasurementKind.BAD_POSTURE);
            final MeasurementAggregate goodPosture = dailySummary.get(MeasurementKind.GOOD_POSTURE);
            final MeasurementAggregate steps = dailySummary.get(MeasurementKind.STEPS);

            if (calories != null) {
                caloriesTextView.setText(getString(R.string.fragment_summary_text_view_calories_text, calories.getSum()));
                caloriesGroup.setVisibility(View.VISIBLE);
            } else {
                caloriesGroup.setVisibility(View.GONE);
            }

            if (distance != null) {
                distanceTextView.setText(getString(R.string.fragment_summary_text_view_distance_text, distance.getSum()));
                distanceGroup.setVisibility(View.VISIBLE);
            } else {
                distanceGroup.setVisibility(View.GONE);
            }

            if (heartRate != null) {
                heartRateTextView.setText(getString(R.string.fragment_summary_text_view_heart_rate_text, heartRate.getAverage()));
                heartrateGroup.setVisibility(View.VISIBLE);
            } else {
                heartrateGroup.setVisibility(View.GONE);
            }
            postureGroup.setVisibility(View.VISIBLE);

            if (badPosture != null || goodPosture != null) {
                postureTextView.setText(getString(R.string.fragment_summary_text_view_posture_text, badPosture == null ? 0 : badPosture.getSum(), goodPosture == null ? 0 : goodPosture.getSum()));
                postureGroup.setVisibility(View.VISIBLE);
            } else {
                postureGroup.setVisibility(View.GONE);
            }

            if (steps != null) {
                stepsTextView.setText(getString(R.string.fragment_summary_text_view_steps_text, steps.getSum()));
                stepsGroup.setVisibility(View.VISIBLE);
            } else {
                stepsGroup.setVisibility(View.GONE);
            }

            if (calories == null && goodPosture == null && distance == null && steps == null && calories == null && heartRate == null) {
                noDataTextView.setText("No activity data for today.");
                noDataGroup.setVisibility(View.VISIBLE); //TODO make own card
            } else {
                noDataGroup.setVisibility(View.GONE); //TODO all other gones
            }
        }
    }
}