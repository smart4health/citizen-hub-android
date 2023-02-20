package pt.uninova.s4h.citizenhub.persistence.repository;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.SampleDao;
import pt.uninova.s4h.citizenhub.persistence.entity.SampleRecord;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class SampleRepository {

    private final SampleDao sampleDao;

    public SampleRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        sampleDao = citizenHubDatabase.sampleDao();
    }

    public void create(Sample sample) {
        CitizenHubDatabase.executorService().execute(() -> sampleDao.insert(sample));
    }

    public void create(Sample sample, Observer<Long> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(sampleDao.insert(sample)));
    }

    public void read(long id, Observer<SampleRecord> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(sampleDao.select(id)));
    }

    public LiveData<Integer> readCount(LocalDate date) {
        return sampleDao.selectCountLiveData(date, date.plusDays(1));
    }

    public void readNonEmptyDates(LocalDate from, LocalDate to, Observer<List<LocalDate>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(sampleDao.select(from, to)));
    }
}