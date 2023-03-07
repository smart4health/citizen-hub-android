package pt.uninova.s4h.citizenhub.ui.summary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.ServiceFragment;
import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;

public class SummaryFragment extends ServiceFragment {

    private SummaryViewModel model;

    private String millisToString(long value) {
        return secondsToString(value / 1000);
    }

    private void onActivityDataUpdate(SummaryViewModel.ActivityData value) {
        final View view = requireView();

        final boolean hasData = value != null && value.hasData();

        if (hasData) {
            updateTextView(view.findViewById(R.id.activityCaloriesValueTextView), R.string.activity_calories_value, value.getCalories());
            updateTextView(view.findViewById(R.id.activityDistanceValueTextView), R.string.activity_distance_value, value.getDistance());
            updateTextView(view.findViewById(R.id.activityStepsValueTextView), R.string.activity_steps_value, value.getSteps());
        }

        updateCardViewVisibility(view.findViewById(R.id.activityCardView), hasData);
    }

    private void onBreathingRateUpdate(SummaryViewModel.BreathingRateData value) {
        final View view = requireView();

        boolean hasData = value != null;

        if (hasData) {
            updateTextView(view.findViewById(R.id.breathingRateValueTextView), R.string.breathing_rate_value, value.getAverage());
        }

        updateCardViewVisibility(view.findViewById(R.id.breathingRateCardView), hasData);
    }

    private void onBloodPressureDataUpdate(SummaryViewModel.BloodPressureData value) {
        final View view = requireView();

        final boolean hasData = value != null && value.hasData();

        if (hasData) {
            updateTextView(view.findViewById(R.id.bloodPressureDiastolicValueTextView), R.string.blood_pressure_diastolic_value, value.getDiastolic());
            updateTextView(view.findViewById(R.id.bloodPressureSystolicValueTextView), R.string.blood_pressure_systolic_value, value.getSystolic());
        }

        updateCardViewVisibility(view.findViewById(R.id.bloodPressureCardView), hasData);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        model = new ViewModelProvider(requireActivity()).get(SummaryViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary_2, container, false);
    }

    private void onHasDataUpdate(Boolean value) {
        final View view = requireView();
        final TextView noDataTextView = view.findViewById(R.id.fragment_summary_text_view_no_data);

        if (value != null && value) {
            noDataTextView.setVisibility(View.GONE);
        } else {
            final AgentOrchestrator agentOrchestrator = getService().getAgentOrchestrator();

            if (agentOrchestrator.getDevices().isEmpty())
                noDataTextView.setText(getString(R.string.fragment_report_text_view_no_data_summary_nodevices));
            else
                noDataTextView.setText(getString(R.string.fragment_report_text_view_no_data_summary));

            noDataTextView.setVisibility(View.VISIBLE);
        }
    }

    private void onHeartRateDataUpdate(SummaryViewModel.HeartRateData value) {
        final View view = requireView();

        final boolean hasData = value != null && value.hasData();

        if (hasData) {
            updateTextView(view.findViewById(R.id.heartRateValueTextView), R.string.heart_rate_value, value.getAverage());
        }

        updateCardViewVisibility(view.findViewById(R.id.heartRateCardView), hasData);
    }

    private void onPostureDataUpdate(SummaryViewModel.PostureData value) {
        final View view = requireView();

        final boolean hasData = value != null && value.hasData();

        if (hasData) {
            updateTextView(view.findViewById(R.id.postureCorrectValueTextView), R.string.posture_correct_value, secondsToString(value.getCorrect()));
            updateTextView(view.findViewById(R.id.postureIncorrectValueTextView), R.string.posture_incorrect_value, secondsToString(value.getIncorrect()));
        }

        updateCardViewVisibility(view.findViewById(R.id.postureCardView), hasData);
    }

    @Override
    public void onResume() {
        super.onResume();

        model.refreshData();
    }

    @Override
    public void onServiceConnected() {
        model.getActivityData().observe(getViewLifecycleOwner(), this::onActivityDataUpdate);
        model.getBloodPressureData().observe(getViewLifecycleOwner(), this::onBloodPressureDataUpdate);
        model.getBreathingRateData().observe(getViewLifecycleOwner(), this::onBreathingRateUpdate);
        model.getHeartRateData().observe(getViewLifecycleOwner(), this::onHeartRateDataUpdate);
        model.getPostureData().observe(getViewLifecycleOwner(), this::onPostureDataUpdate);
        model.hasData().observe(getViewLifecycleOwner(), this::onHasDataUpdate);
        model.getLumbarExtensionTrainingData().observe(getViewLifecycleOwner(), this::onDailyLumbarExtensionTrainingUpdate);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.activityCardView).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_summary_fragment_to_summary_detail_activity_fragment));
        view.findViewById(R.id.bloodPressureCardView).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_summary_fragment_to_summary_detail_blood_pressure_fragment));
        view.findViewById(R.id.heartRateCardView).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_summary_fragment_to_summary_detail_heart_rate_fragment));
        view.findViewById(R.id.lumbarTrainingCardView).setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.action_summary_fragment_to_summary_detail_lumbar_extension_fragment));
        view.findViewById(R.id.postureCardView).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_summary_fragment_to_summary_detail_posture_fragment));
    }

    private void updateCardViewVisibility(CardView view, boolean visible) {
        if (visible) {
            view.setVisibility(View.VISIBLE);
            view.setClickable(true);
        } else {
            view.setClickable(false);
            view.setVisibility(View.GONE);
        }
    }

    private void updateTextView(TextView view, int resId, Object args) {
        if (args == null) {
            view.setVisibility(View.GONE);
            view.setText("");
        } else {
            view.setText(getString(resId, args));
            view.setVisibility(View.VISIBLE);
        }
    }

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

///////////////////////////////////

    private void onDailyLumbarExtensionTrainingUpdate(SummaryViewModel.LumbarExtensionTrainingData record) {
        final View view = requireView();
        final CardView lumbarExtensionTrainingCardView = view.findViewById(R.id.lumbarTrainingCardView);

        if (record == null) {
            lumbarExtensionTrainingCardView.setVisibility(View.GONE);
        } else {
            final TextView lumbarExtensionTrainingCaloriesTextView = view.findViewById(R.id.lumbarTrainingCaloriesValueTextView);
            final TextView lumbarExtensionTrainingDurationTextView = view.findViewById(R.id.lumbarTrainingDurationValueTextView);
            final TextView lumbarExtensionTrainingRepetitionsTextView = view.findViewById(R.id.lumbarTrainingRepetitionsValueTextView);
            final TextView lumbarExtensionTrainingWeightTextView = view.findViewById(R.id.lumbarTrainingWeightValueTextView);

            if (record.getCalories() == null) {
                lumbarExtensionTrainingCaloriesTextView.setVisibility(View.GONE);
            } else {
                lumbarExtensionTrainingCaloriesTextView.setText(getString(R.string.lumbar_training_calories_value, record.getCalories()));
                lumbarExtensionTrainingCaloriesTextView.setVisibility(View.VISIBLE);
            }

            lumbarExtensionTrainingDurationTextView.setText(getString(R.string.lumbar_training_duration_value, millisToString(record.getDuration().toMillis())));
            lumbarExtensionTrainingRepetitionsTextView.setText(getString(R.string.lumbar_training_repetitions_value, record.getRepetitions()));
            lumbarExtensionTrainingWeightTextView.setText(getString(R.string.lumbar_training_weight_value, record.getTrainingWeight()));
            updateCardViewVisibility(lumbarExtensionTrainingCardView, true);
        }
    }
}
