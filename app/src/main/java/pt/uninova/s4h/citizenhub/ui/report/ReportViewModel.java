package pt.uninova.s4h.citizenhub.ui.report;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pt.uninova.s4h.citizenhub.persistence.entity.util.AggregateSummary;
import pt.uninova.s4h.citizenhub.persistence.entity.util.PostureClassificationSum;
import pt.uninova.s4h.citizenhub.persistence.repository.HeartRateMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.PostureMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.ReportRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.SampleRepository;
import pt.uninova.s4h.citizenhub.report.Report;
import pt.uninova.s4h.citizenhub.report.ReportGenerator;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class ReportViewModel extends AndroidViewModel {

    private LocalDate currentDate;

    private final HeartRateMeasurementRepository heartRateMeasurementRepository;
    private final PostureMeasurementRepository postureMeasurementRepository;
    private final SampleRepository sampleRepository;

    private final MutableLiveData<Set<LocalDate>> dateSet;

    private LiveData<Integer> dailyDataExistence;
    private LiveData<List<PostureClassificationSum>> dailyPostureMeasurement;
    private LiveData<AggregateSummary> dailyHeartRateSummary;

    public ReportViewModel(Application application) {
        super(application);

        currentDate = LocalDate.now();

        dateSet = new MutableLiveData<>();

        heartRateMeasurementRepository = new HeartRateMeasurementRepository(application);
        postureMeasurementRepository = new PostureMeasurementRepository(application);
        sampleRepository = new SampleRepository(application);


        final int year = currentDate.getYear();
        final int month = currentDate.getMonthValue();

        setMonthView(year, month);
    }

    public void getWorkTimeReport(Application application, boolean pdf, Observer<Report> observerWorkTimeReport){
        ReportGenerator reportGenerator = new ReportGenerator(getApplication().getBaseContext());
        reportGenerator.generateWorkTimeReport(new ReportRepository(application.getApplicationContext()), currentDate, pdf, observerWorkTimeReport);
    }

    public void getNotWorkTimeReport(Application application, boolean pdf, Observer<Report> observerNotWorkTimeReport){
        ReportGenerator reportGenerator = new ReportGenerator(getApplication().getBaseContext());
        reportGenerator.generateNotWorkTimeReport(new ReportRepository(application.getApplicationContext()), currentDate, pdf, observerNotWorkTimeReport);

    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public LiveData<Integer> getDailyDataExistence() {
        return dailyDataExistence;
    }

    public LiveData<AggregateSummary> getDailyHeartRateSummary() {
        return dailyHeartRateSummary;
    }

    public LiveData<List<PostureClassificationSum>> getDailyPostureMeasurement() {
        return dailyPostureMeasurement;
    }

    public LiveData<Set<LocalDate>> getDateSet() {
        return dateSet;
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;

        dailyDataExistence = sampleRepository.readCount(currentDate);
        dailyHeartRateSummary = heartRateMeasurementRepository.readAggregate(currentDate);
        dailyPostureMeasurement = postureMeasurementRepository.readClassificationSum(currentDate);
    }

    public void setMonthView(int year, int month) {
        sampleRepository.readNonEmptyDates(LocalDate.of(year, month, 1), LocalDate.of(month == 12 ? year + 1 : year, month == 12 ? 1 : month + 1, 1), dateList -> {
            dateSet.postValue(new HashSet<>(dateList));
        });
    }
}
