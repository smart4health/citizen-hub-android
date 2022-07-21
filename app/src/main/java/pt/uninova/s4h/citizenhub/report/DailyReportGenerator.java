package pt.uninova.s4h.citizenhub.report;

import android.content.Context;
import android.content.res.Resources;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.data.Measurement;
import pt.uninova.s4h.citizenhub.localization.MeasurementKindLocalization;
import pt.uninova.s4h.citizenhub.persistence.entity.util.ReportUtil;
import pt.uninova.s4h.citizenhub.persistence.repository.ReportRepository;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class DailyReportGenerator {

    private final Resources resources;
    private final MeasurementKindLocalization localization;

    public DailyReportGenerator(Context context) {
        this.resources = context.getResources();
        this.localization = new MeasurementKindLocalization(context);
    }

    private void groupSimpleRecords(ReportUtil reportUtil, List<Group> groups) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        System.out.println("Entered Simples Daily Reports.");
        if (reportUtil.getCalories() != null || reportUtil.getDistance() != null || reportUtil.getSteps() != null) {
            //StringMeasurementId label = new StringMeasurementId(Measurement.TYPE_CALORIES, localization);
            MeasurementTypeLocalizedResource label = new MeasurementTypeLocalizedResource(localization, Measurement.TYPE_DISTANCE_SNAPSHOT);
            Group groupActivity = new Group(label);
            if (reportUtil.getSteps() != null) {
                groupActivity.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_steps_label)), new ResourceValue(decimalFormat.format(reportUtil.getSteps())), new ResourceUnits("-")));
            }
            if (reportUtil.getDistance() != null) {
                groupActivity.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_distance_label)), new ResourceValue(decimalFormat.format(reportUtil.getDistance())), new ResourceUnits(resources.getString(R.string.report_distance_units))));
            }
            if (reportUtil.getCalories() != null) {
                groupActivity.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_calories_label)), new ResourceValue(decimalFormat.format(reportUtil.getCalories())), new ResourceUnits(resources.getString(R.string.report_calories_units))));
            }
            groups.add(groupActivity);
        }
        /*if(reportUtil.getCalories()!=null){
            StringMeasurementId label = new StringMeasurementId(Measurement.TYPE_CALORIES, localization);
            Group groupCalories = new Group(label);
            groupCalories.getItemList().add(new Item(new StringType("Calories"), new StringValue(reportUtil.getCalories().toString())));
            groups.add(groupCalories);
        }
        if(reportUtil.getDistance()!=null){
            StringMeasurementId label = new StringMeasurementId(Measurement.TYPE_DISTANCE_SNAPSHOT, localization);
            Group groupDistance = new Group(label);
            groupDistance.getItemList().add(new Item(new StringType("Distance"), new StringValue(reportUtil.getDistance().toString())));
            groups.add(groupDistance);
        }
        if(reportUtil.getSteps()!=null){
            StringMeasurementId label = new StringMeasurementId(Measurement.TYPE_STEPS_SNAPSHOT, localization);
            Group groupSteps = new Group(label);
            groupSteps.getItemList().add(new Item(new StringType("Steps"), new StringValue(reportUtil.getSteps().toString())));
            groups.add(groupSteps);
        }*/
        if (reportUtil.getMaxBreathingRate() != null && reportUtil.getMinBreathingRate() != null && reportUtil.getAvgBreathingRate() != null) {
            MeasurementTypeLocalizedResource label = new MeasurementTypeLocalizedResource(localization, Measurement.TYPE_BREATHING_RATE);
            Group groupBreathingRate = new Group(label);
            groupBreathingRate.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_br_average_label)), new ResourceValue(decimalFormat.format(reportUtil.getAvgBreathingRate())), new ResourceUnits(resources.getString(R.string.report_br_units))));
            groupBreathingRate.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_br_maximum_label)), new ResourceValue(decimalFormat.format(reportUtil.getMaxBreathingRate())), new ResourceUnits(resources.getString(R.string.report_br_units))));
            groupBreathingRate.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_br_minimum_label)), new ResourceValue(decimalFormat.format(reportUtil.getMinBreathingRate())), new ResourceUnits(resources.getString(R.string.report_br_units))));
            groups.add(groupBreathingRate);
        }
        if (reportUtil.getMaxHeartRate() != null && reportUtil.getMinHeartRate() != null && reportUtil.getAvgHeartRate() != null) {
            MeasurementTypeLocalizedResource label = new MeasurementTypeLocalizedResource(localization, Measurement.TYPE_HEART_RATE);
            Group groupHeartRate = new Group(label);
            groupHeartRate.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_hr_average_label)), new ResourceValue(decimalFormat.format(reportUtil.getAvgHeartRate())), new ResourceUnits(resources.getString(R.string.report_hr_units))));
            groupHeartRate.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_hr_maximum_label)), new ResourceValue(decimalFormat.format(reportUtil.getMaxHeartRate())), new ResourceUnits(resources.getString(R.string.report_hr_units))));
            groupHeartRate.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_hr_minimum_label)), new ResourceValue(decimalFormat.format(reportUtil.getMinHeartRate())), new ResourceUnits(resources.getString(R.string.report_hr_units))));
            groups.add(groupHeartRate);
        }
        if (reportUtil.getCorrectPostureDuration() != null) {
            MeasurementTypeLocalizedResource label = new MeasurementTypeLocalizedResource(localization, Measurement.TYPE_POSTURE);
            Group groupPosture = new Group(label);
            groupPosture.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_correct_posture_label)), new ResourceValue(secondsToString(reportUtil.getCorrectPostureDuration().getSeconds())), new ResourceUnits("-")));
            if (reportUtil.getWrongPostureDuration() != null) {
                groupPosture.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_incorrect_posture_label)), new ResourceValue(secondsToString(reportUtil.getWrongPostureDuration().getSeconds())), new ResourceUnits("-")));
            } else {
                groupPosture.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_incorrect_posture_label)), new ResourceValue("0s"), new ResourceUnits("-")));
            }
            groups.add(groupPosture);
        } else {
            if (reportUtil.getWrongPostureDuration() != null) {
                MeasurementTypeLocalizedResource label = new MeasurementTypeLocalizedResource(localization, Measurement.TYPE_POSTURE);
                Group groupPosture = new Group(label);
                groupPosture.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_correct_posture_label)), new ResourceValue("0s"), new ResourceUnits("-")));
                groupPosture.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_incorrect_posture_label)), new ResourceValue(secondsToString(reportUtil.getWrongPostureDuration().getSeconds())), new ResourceUnits("-")));
                groups.add(groupPosture);
            }
        }
    }

    private void groupBloodPressure(List<ReportUtil> observeBloodPressure, List<Group> groups) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        System.out.println("Entered Blood Pressure.");
        if (observeBloodPressure.size() > 0) {
            MeasurementTypeLocalizedResource label = new MeasurementTypeLocalizedResource(localization, Measurement.TYPE_BLOOD_PRESSURE);
            Group groupBloodPressure = new Group(label);
            for (ReportUtil reportUtil : observeBloodPressure) {
                TimestampLocalizedResource timestamp = new TimestampLocalizedResource(reportUtil.getTimestamp());
                Group group = new Group(timestamp);
                group.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_bp_average_label)), new ResourceValue(decimalFormat.format(reportUtil.getMeanArterialPressure())), new ResourceUnits(resources.getString(R.string.report_bp_units))));
                group.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_bp_diastolic_label)), new ResourceValue(decimalFormat.format(reportUtil.getDiastolic())), new ResourceUnits(resources.getString(R.string.report_bp_units))));
                group.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_bp_systolic_label)), new ResourceValue(decimalFormat.format(reportUtil.getSystolic())), new ResourceUnits(resources.getString(R.string.report_bp_units))));
                group.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_pulse_rate)), new ResourceValue(decimalFormat.format(reportUtil.getPulseRate())), new ResourceUnits(resources.getString(R.string.report_hr_units))));
                groupBloodPressure.getGroupList().add(group);
            }
            groups.add(groupBloodPressure);
        }
    }

    private void groupLumbarExtensionTraining(List<ReportUtil> observerLumbarExtension, List<Group> groups) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        System.out.println("Entered Lumbar Extension.");
        if (observerLumbarExtension.size() > 0) {
            MeasurementTypeLocalizedResource label = new MeasurementTypeLocalizedResource(localization, Measurement.TYPE_LUMBAR_EXTENSION_TRAINING);
            Group groupLumbarExtension = new Group(label);
            for (ReportUtil reportUtil : observerLumbarExtension) {
                TimestampLocalizedResource timestamp = new TimestampLocalizedResource(reportUtil.getTimestamp());
                Group group = new Group(timestamp);
                System.out.println("Report: " + reportUtil.getLumbarExtensionDuration());
                System.out.println("Report: " + reportUtil.getLumbarExtensionDuration().getSeconds());
                group.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_lumbar_training_score_label)), new ResourceValue(decimalFormat.format(reportUtil.getLumbarExtensionScore())), new ResourceUnits(resources.getString(R.string.report_lumbar_training_score_units))));
                group.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_lumbar_training_repetitions_label)), new ResourceValue(decimalFormat.format(reportUtil.getLumbarExtensionRepetitions())), new ResourceUnits("-")));
                group.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_lumbar_training_weight_label)), new ResourceValue(decimalFormat.format(reportUtil.getLumbarExtensionWeight())), new ResourceUnits(resources.getString(R.string.report_lumbar_training_weight_units))));
                group.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_lumbar_training_duration_label)), new ResourceValue(secondsToString(reportUtil.getLumbarExtensionDuration().getSeconds())), new ResourceUnits("-")));
                if (reportUtil.getCalories() != null) {
                    group.getItemList().add(new Item(new ResourceType(resources.getString(R.string.report_calories_label)), new ResourceValue(decimalFormat.format(reportUtil.getCalories())), new ResourceUnits(resources.getString(R.string.report_calories_units))));
                }
                groupLumbarExtension.getGroupList().add(group);
            }
            groups.add(groupLumbarExtension);
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

    public void generateWorkTimeReport(ReportRepository reportRepository, LocalDate date, Observer<Report> observerReport) {

        Report report = new Report(() -> "Work Time Daily Report", new LocalDateLocalizedResource(date));
        List<Group> groups = report.getGroups();

        //Simple Records
        reportRepository.getWorkTimeSimpleRecords(date, observerSimpleRecords -> {
            groupSimpleRecords(observerSimpleRecords, groups);
            //Blood Pressure
            reportRepository.getWorkTimeBloodPressure(date, observerBloodPressure -> {
                groupBloodPressure(observerBloodPressure, groups);
                //Lumbar Extension Training
                reportRepository.getWorkTimeLumbarExtensionTraining(date, observerLumbarExtension -> {
                    groupLumbarExtensionTraining(observerLumbarExtension, groups);
                    observerReport.observe(report);
                });
            });
        });

    }

    public void generateNotWorkTimeReport(ReportRepository reportRepository, LocalDate date, Observer<Report> observerReport) {

        Report report = new Report(() -> "Work Time Daily Report", new LocalDateLocalizedResource(date));
        List<Group> groups = report.getGroups();

        //Simple Records
        reportRepository.getNotWorkTimeSimpleRecords(date, observerSimpleRecords -> {
            groupSimpleRecords(observerSimpleRecords, groups);
            //Blood Pressure
            reportRepository.getNotWorkTimeBloodPressure(date, observerBloodPressure -> {
                groupBloodPressure(observerBloodPressure, groups);
                //Lumbar Extension Training
                reportRepository.getNotWorkTimeLumbarExtensionTraining(date, observerLumbarExtension -> {
                    groupLumbarExtensionTraining(observerLumbarExtension, groups);
                    observerReport.observe(report);
                });
            });
        });

    }

}
