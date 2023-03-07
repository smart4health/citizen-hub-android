package pt.uninova.s4h.citizenhub.report;

import android.content.Context;
import android.content.res.Resources;

import java.text.DecimalFormat;
import java.util.List;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.data.BloodPressureValue;
import pt.uninova.s4h.citizenhub.data.CaloriesMeasurement;
import pt.uninova.s4h.citizenhub.data.LumbarExtensionTrainingValue;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.localization.MeasurementKindLocalization;

public class Smart4HealthReportGenerator {

    private final MeasurementKindLocalization measurementKindLocalization;
    private final Resources resources;

    public Smart4HealthReportGenerator(Context context) {
        measurementKindLocalization = new MeasurementKindLocalization(context);
        resources = context.getResources();
    }

    private Group createBloodPressureGroup(Sample sample) {
        final DecimalFormat decimalFormat = new DecimalFormat("#.##");

        final MeasurementTypeLocalizedResource title = new MeasurementTypeLocalizedResource(measurementKindLocalization, Measurement.TYPE_BLOOD_PRESSURE);
        final Group mainGroup = new Group(title);

        final TimestampLocalizedResource timestamp = new TimestampLocalizedResource(sample.getTimestamp());
        final Group measurementGroup = new Group(timestamp);

        mainGroup.getGroupList().add(measurementGroup);

        final List<Item> measurementGroupItemList = measurementGroup.getItemList();

        final Measurement<?>[] measurements = sample.getMeasurements();
        final BloodPressureValue val = (BloodPressureValue) measurements[0].getValue();

        measurementGroupItemList.add(new Item(new ResourceType(resources.getString(R.string.report_bp_systolic_label)), new ResourceValue(decimalFormat.format(val.getSystolic())), new ResourceUnits(resources.getString(R.string.report_bp_units))));
        measurementGroupItemList.add(new Item(new ResourceType(resources.getString(R.string.report_bp_diastolic_label)), new ResourceValue(decimalFormat.format(val.getDiastolic())), new ResourceUnits(resources.getString(R.string.report_bp_units))));
        measurementGroupItemList.add(new Item(new ResourceType(resources.getString(R.string.report_bp_average_label)), new ResourceValue(decimalFormat.format(val.getMeanArterialPressure())), new ResourceUnits(resources.getString(R.string.report_bp_units))));

        final Double pulse = measurements.length > 1 ? (Double) measurements[1].getValue() : null;

        if (pulse != null) {
            measurementGroupItemList.add(new Item(new ResourceType(resources.getString(R.string.report_pulse_rate)), new ResourceValue(decimalFormat.format(pulse)), new ResourceUnits(resources.getString(R.string.report_hr_units))));
        }

        return mainGroup;
    }

    public Report createBloodPressureReport(Sample sample) {
        final Report report = new Report(new MeasurementTypeLocalizedResource(measurementKindLocalization, Measurement.TYPE_BLOOD_PRESSURE), new TimestampLocalizedResource(sample.getTimestamp()));

        report.getGroups().add(createBloodPressureGroup(sample));

        return report;
    }

    private Group createLumbarExtensionTrainingGroup(Sample sample) {
        final DecimalFormat decimalFormat = new DecimalFormat("#.##");

        final MeasurementTypeLocalizedResource title = new MeasurementTypeLocalizedResource(measurementKindLocalization, Measurement.TYPE_LUMBAR_EXTENSION_TRAINING);
        final Group lumbarExtensionGroup = new Group(title);

        final TimestampLocalizedResource timestamp = new TimestampLocalizedResource(sample.getTimestamp());
        final Group measurementGroup = new Group(timestamp);

        lumbarExtensionGroup.getGroupList().add(measurementGroup);

        final List<Item> measurementGroupItemList = measurementGroup.getItemList();

        final Measurement<?>[] measurements = sample.getMeasurements();
        final LumbarExtensionTrainingValue val = (LumbarExtensionTrainingValue) measurements[0].getValue();

        measurementGroupItemList.add(new Item(new ResourceType(resources.getString(R.string.report_lumbar_training_score_label)), new ResourceValue(decimalFormat.format(val.getScore())), new ResourceUnits(resources.getString(R.string.report_lumbar_training_score_units))));
        measurementGroupItemList.add(new Item(new ResourceType(resources.getString(R.string.report_lumbar_training_repetitions_label)), new ResourceValue(decimalFormat.format(val.getRepetitions())), new ResourceUnits("")));
        measurementGroupItemList.add(new Item(new ResourceType(resources.getString(R.string.report_lumbar_training_weight_label)), new ResourceValue(decimalFormat.format(val.getWeight())), new ResourceUnits(resources.getString(R.string.report_lumbar_training_weight_units))));
        measurementGroupItemList.add(new Item(new ResourceType(resources.getString(R.string.report_lumbar_training_duration_label)), new ResourceValue(secondsToString(val.getDuration().getSeconds())), new ResourceUnits("")));

        final double calories = ((CaloriesMeasurement) measurements[1]).getValue();

        measurementGroupItemList.add(new Item(new ResourceType(resources.getString(R.string.report_calories_label)), new ResourceValue(decimalFormat.format(calories)), new ResourceUnits(resources.getString(R.string.report_calories_units))));

        return lumbarExtensionGroup;
    }

    public Report createLumbarExtensionTrainingReport(Sample sample) {
        final Report report = new Report(new MeasurementTypeLocalizedResource(measurementKindLocalization, Measurement.TYPE_LUMBAR_EXTENSION_TRAINING), new TimestampLocalizedResource(sample.getTimestamp()));

        report.getGroups().add(createLumbarExtensionTrainingGroup(sample));

        return report;
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
}
