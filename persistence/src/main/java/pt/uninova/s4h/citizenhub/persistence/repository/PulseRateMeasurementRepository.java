package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.PulseRateMeasurementDao;
import pt.uninova.s4h.citizenhub.persistence.entity.PulseRateMeasurementRecord;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class PulseRateMeasurementRepository {

    private final PulseRateMeasurementDao pulseRateMeasurementDao;

    public PulseRateMeasurementRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        pulseRateMeasurementDao = citizenHubDatabase.pulseRateMeasurementDao();
    }

    public void create(PulseRateMeasurementRecord record) {
        CitizenHubDatabase.executorService().execute(() -> pulseRateMeasurementDao.insert(record));
    }

    public LiveData<List<PulseRateMeasurementRecord>> read(LocalDate localDate) {
        return pulseRateMeasurementDao.selectLiveData(localDate, localDate.plusDays(1));
    }

    public LiveData<Double> readAverage(LocalDate localDate) {
        return pulseRateMeasurementDao.selectAverageLiveData(localDate, localDate.plusDays(1));
    }

}