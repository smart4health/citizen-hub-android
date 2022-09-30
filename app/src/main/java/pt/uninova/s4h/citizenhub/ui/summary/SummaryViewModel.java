package pt.uninova.s4h.citizenhub.ui.summary;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.persistence.entity.BloodPressureMeasurementRecord;
import pt.uninova.s4h.citizenhub.persistence.entity.util.LumbarExtensionTrainingSummary;
import pt.uninova.s4h.citizenhub.persistence.entity.util.PostureClassificationSum;
import pt.uninova.s4h.citizenhub.persistence.entity.util.SummaryDetailUtil;
import pt.uninova.s4h.citizenhub.persistence.repository.BloodPressureMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.BreathingRateMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.CaloriesMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.DistanceMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.HeartRateMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.LumbarExtensionTrainingRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.PostureMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.SampleRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.StepsMeasurementRepository;

public class SummaryViewModel extends AndroidViewModel {

    private final LiveData<List<BloodPressureMeasurementRecord>> dailyBloodPressureMeasurement;
    private final LiveData<Double> dailyBreathingRate;
    private final LiveData<Integer> dailyDataExistence;
    private final LiveData<Double> dailyHeartRate;
    private final LiveData<LumbarExtensionTrainingSummary> dailyLumbarExtensionTraining;
    private final LiveData<List<PostureClassificationSum>> dailyPostureMeasurement;
    private final LiveData<Integer> dailyStepsAllTypes;
    private final LiveData<Double> dailyDistanceAllTypes;
    private final LiveData<Double> dailyCaloriesAllTypes;

    public SummaryViewModel(Application application) {
        super(application);

        BloodPressureMeasurementRepository bloodPressureMeasurementRepository = new BloodPressureMeasurementRepository(application);
        BreathingRateMeasurementRepository breathingRateMeasurementRepository = new BreathingRateMeasurementRepository(application);
        HeartRateMeasurementRepository heartRateMeasurementRepository = new HeartRateMeasurementRepository(application);
        LumbarExtensionTrainingRepository lumbarExtensionTrainingRepository = new LumbarExtensionTrainingRepository(application);
        PostureMeasurementRepository postureMeasurementRepository = new PostureMeasurementRepository(application);
        SampleRepository sampleRepository = new SampleRepository(application);
        StepsMeasurementRepository stepsMeasurementRepository = new StepsMeasurementRepository(application);
        DistanceMeasurementRepository distanceMeasurementRepository = new DistanceMeasurementRepository(application);
        CaloriesMeasurementRepository caloriesMeasurementRepository = new CaloriesMeasurementRepository(application);

        final LocalDate now = LocalDate.now();

        dailyLumbarExtensionTraining = lumbarExtensionTrainingRepository.readLatest(now);
        dailyBloodPressureMeasurement = bloodPressureMeasurementRepository.read(now);
        dailyBreathingRate = breathingRateMeasurementRepository.readAverage(now);
        dailyDataExistence = sampleRepository.readCount(now);
        dailyHeartRate = heartRateMeasurementRepository.readAverage(now);
        dailyPostureMeasurement = postureMeasurementRepository.readClassificationSum(now);
        dailyStepsAllTypes = stepsMeasurementRepository.getStepsAllTypes(now);
        dailyDistanceAllTypes = distanceMeasurementRepository.getDistanceAllTypes(now);
        dailyCaloriesAllTypes = caloriesMeasurementRepository.getCaloriesAllTypes(now);
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

    public LiveData<Integer> getDailyStepsAllTypes() {return dailyStepsAllTypes;}

    public LiveData<Double> getDailyDistanceAllTypes(){return dailyDistanceAllTypes;}

    public LiveData<Double> getDailyCaloriesAllTypes(){return dailyCaloriesAllTypes;}

    public ChartMarkerView getChartViewMarker(){return new ChartMarkerView(getApplication().getBaseContext(), R.layout.fragment_summary_detail_line_chart_marker);}

}
