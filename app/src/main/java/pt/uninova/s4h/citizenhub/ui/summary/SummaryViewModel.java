package pt.uninova.s4h.citizenhub.ui.summary;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.entity.BloodPressureMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailUtil;
import pt.uninova.s4h.citizenhub.persistence.entity.util.LumbarExtensionTrainingSummary;
import pt.uninova.s4h.citizenhub.persistence.entity.util.WalkingInformation;
import pt.uninova.s4h.citizenhub.persistence.repository.BloodPressureMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.BreathingRateMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.HeartRateMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.LumbarExtensionTrainingRepository;
import pt.uninova.s4h.citizenhub.persistence.entity.util.PostureClassificationSum;
import pt.uninova.s4h.citizenhub.persistence.repository.PostureMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.SampleRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.StepsSnapshotMeasurementRepository;

public class SummaryViewModel extends AndroidViewModel {

    private final LiveData<List<BloodPressureMeasurementRecord>> dailyBloodPressureMeasurement;
    private final LiveData<Double> dailyBreathingRate;
    private final LiveData<Integer> dailyDataExistence;
    private final LiveData<Double> dailyHeartRate;
    private final LiveData<LumbarExtensionTrainingSummary> dailyLumbarExtensionTraining;
    private final LiveData<List<PostureClassificationSum>> dailyPostureMeasurement;
    private final LiveData<WalkingInformation> dailyWalkingInformation;

    public SummaryViewModel(Application application) {
        super(application);

        BloodPressureMeasurementRepository bloodPressureMeasurementRepository = new BloodPressureMeasurementRepository(application);
        BreathingRateMeasurementRepository breathingRateMeasurementRepository = new BreathingRateMeasurementRepository(application);
        HeartRateMeasurementRepository heartRateMeasurementRepository = new HeartRateMeasurementRepository(application);
        LumbarExtensionTrainingRepository lumbarExtensionTrainingRepository = new LumbarExtensionTrainingRepository(application);
        PostureMeasurementRepository postureMeasurementRepository = new PostureMeasurementRepository(application);
        SampleRepository sampleRepository = new SampleRepository(application);
        StepsSnapshotMeasurementRepository stepsSnapshotMeasurementRepository = new StepsSnapshotMeasurementRepository(application);

        final LocalDate now = LocalDate.now();

        dailyLumbarExtensionTraining = lumbarExtensionTrainingRepository.readLatest(now);
        dailyBloodPressureMeasurement = bloodPressureMeasurementRepository.read(now);
        dailyBreathingRate = breathingRateMeasurementRepository.readAverage(now);
        dailyDataExistence = sampleRepository.readCount(now);
        dailyHeartRate = heartRateMeasurementRepository.readAverage(now);
        dailyPostureMeasurement = postureMeasurementRepository.readClassificationSum(now);
        dailyWalkingInformation = stepsSnapshotMeasurementRepository.readLatestWalkingInformation(now);
    }

    public LiveData<LumbarExtensionTrainingSummary> getDailyLumbarExtensionTraining() {
        return dailyLumbarExtensionTraining;
    }

    public LiveData<List<BloodPressureMeasurementRecord>> getDailyBloodPressureMeasurement() {
        return dailyBloodPressureMeasurement;
    }

    public LiveData<Double> getDailyBreathingRateMeasurement() {
        return dailyBreathingRate;
    }

    public LiveData<Integer> getDailyDataExistence() {
        return dailyDataExistence;
    }

    public LiveData<Double> getDailyHeartRate() {
        return dailyHeartRate;
    }

    public LiveData<List<PostureClassificationSum>> getDailyPostureMeasurement() {
        return dailyPostureMeasurement;
    }

    public LiveData<WalkingInformation> getDailyWalkingInformation() {
        return dailyWalkingInformation;
    }

    public void setupBarChart(BarChart barChart) {
        barChart.setDrawGridBackground(false);
        barChart.setScaleEnabled(true);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setDrawGridLines(false);

        YAxis yAxisRight = barChart.getAxisRight();
        yAxisRight.setEnabled(false);
    }

    public void setupLineChart(LineChart lineChart) {
        lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setText("Steps");

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setDrawGridLines(false);

        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);
    }

    public void setBarChartData(List<SummaryDetailUtil> list, BarChart barChart, String label, int max) {
        List<BarEntry> entries = new ArrayList<>();
        int currentTime = 1;

        for (SummaryDetailUtil data : list) {
            if (data.getTime() + 1 != currentTime) {
                while (currentTime != data.getTime()) {
                    entries.add(new BarEntry(currentTime, 0));
                    currentTime++;
                }
            }
            entries.add(new BarEntry(data.getTime() + 1, data.getValue()));
            currentTime++;
        }

        if (currentTime != max) {
            while (currentTime != max) {
                entries.add(new BarEntry(currentTime, 0));
                currentTime++;
            }
        }

        BarDataSet barDataSet = new BarDataSet(entries, label);
        ArrayList<IBarDataSet> dataSet = new ArrayList<>();
        dataSet.add(barDataSet);
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getDescription().setText(label);
        barChart.notifyDataSetChanged();
        barChart.invalidate();
    }

    public void setLineChartData(List<SummaryDetailUtil> list, LineChart lineChart, String label, int max) {
        List<Entry> entries = new ArrayList<>();
        int currentTime = 1;

        for (SummaryDetailUtil data : list) {
            //System.out.println(data.getValue() + " " + data.getTime());
            if (currentTime != data.getTime()) {
                while (currentTime != data.getTime()) {
                    currentTime++;
                    entries.add(new BarEntry(currentTime, 0));
                }
            }
            entries.add(new BarEntry(data.getTime() + 1, data.getValue()));
            currentTime++;
        }

        if (currentTime != max) {
            while (currentTime != max) {
                currentTime++;
                entries.add(new BarEntry(currentTime, 0));
            }
        }

        LineDataSet lineDataSet = new LineDataSet(entries, label);
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        lineDataSet.setDrawFilled(true);
        ArrayList<ILineDataSet> dataSet = new ArrayList<>();
        dataSet.add(lineDataSet);
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setText(label);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }
}
