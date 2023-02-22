package pt.uninova.s4h.citizenhub.persistence.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.data.Sample;
import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.SampleDao;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Repository used to call queries from the sample dao. */
public class SampleRepository {

    private final SampleDao sampleDao;

    public SampleRepository(Application application) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(application);

        sampleDao = citizenHubDatabase.sampleDao();
    }

    /** Inserts a new sample into the database.
     * @param sample Sample to be inserted.
     * */
    public void create(Sample sample) {
        CitizenHubDatabase.executorService().execute(() -> sampleDao.insert(sample));
    }

    /** Inserts a new sample into the database.
     * @param sample Sample to be inserted.
     * @param observer
     * */
    public void create(Sample sample, Observer<Long> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(sampleDao.insert(sample)));
    }

    /** Selects number of samples retrieved in one day.
     * @param date Date.
     * */
    public LiveData<Integer> readCount(LocalDate date) {
        return sampleDao.selectCountLiveData(date, date.plusDays(1));
    }

    /** Selects days with samples.
     * @param from Starting day.
     * @param to Ending day.
     * @param observer
     * */
    public void readNonEmptyDates(LocalDate from, LocalDate to, Observer<List<LocalDate>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(sampleDao.select(from, to)));
    }

    /** Select the timestamp form a specific sample.
     * @param sampleId Id of a sample.
     * @param observer
     * */
    public void selectTimestampBasedOnId(Long sampleId, Observer<Long> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(sampleDao.selectTimestampBasedOnId(sampleId)));
    }
}