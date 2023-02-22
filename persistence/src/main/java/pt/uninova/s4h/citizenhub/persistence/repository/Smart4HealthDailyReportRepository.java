package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.Smart4HealthDailyReportDao;
import pt.uninova.s4h.citizenhub.persistence.entity.Smart4HealthDailyReportRecord;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Repository used to call queries from the smart4health daily report dao. */
public class Smart4HealthDailyReportRepository {

    private final Smart4HealthDailyReportDao dao;

    public Smart4HealthDailyReportRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        dao = citizenHubDatabase.smart4HealthDailyReportDao();
    }

    /** Inserts a new sample into the database.
     * @param record Sample to be inserted.
     * */
    public void create(Smart4HealthDailyReportRecord record) {
        CitizenHubDatabase.executorService().execute(() -> dao.insert(record));
    }

    /** Inserts a new sample into the database.
     * @param record Sample to be inserted.
     * @param observer
     * */
    public void create(Smart4HealthDailyReportRecord record, Observer<Long> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(dao.insert(record)));
    }

    /** Inserts or updates a given record fhir attribute.
     * @param date Record date.
     * @param value New boolean value.
     * */
    public void createOrUpdateFhir(LocalDate date, Boolean value) {
        CitizenHubDatabase.executorService().execute(() -> dao.insertOrReplaceFhir(date, value));
    }

    /** Inserts or updates a given record pdf attribute.
     * @param date Record date.
     * @param value New boolean value.
     * */
    public void createOrUpdatePdf(LocalDate date, Boolean value) {
        CitizenHubDatabase.executorService().execute(() -> dao.insertOrReplacePdf(date, value));
    }

    /** Inserts or updates a given record fhir UTC attribute.
     * @param date Record date.
     * @param value New boolean value.
     * */
    public void createOrUpdatePdfUTC(LocalDate date, Boolean value) {
        CitizenHubDatabase.executorService().execute(() -> dao.insertOrReplacePdf(date.toEpochDay() * 86400000, value));
    }

    /** Selects days with data.
     * @param observer
     * */
    public void readDaysWithData(Observer<List<LocalDate>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(dao.selectDaysWithValues()));
    }
}
