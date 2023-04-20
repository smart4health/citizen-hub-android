package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.Smart4HealthWeeklyReportDao;
import pt.uninova.s4h.citizenhub.persistence.entity.Smart4HealthWeeklyReportRecord;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Repository used to call queries from the smart4health weekly report dao. */
public class Smart4HealthWeeklyReportRepository {

    private final Smart4HealthWeeklyReportDao dao;

    public Smart4HealthWeeklyReportRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        dao = citizenHubDatabase.smart4HealthWeeklyReportDao();
    }

    /** Inserts a new record into the database.
     * @param record Sample.
     * */
    public void create(Smart4HealthWeeklyReportRecord record) {
        CitizenHubDatabase.executorService().execute(() -> dao.insert(record));
    }

    /** Inserts a new record into the database.
     * @param record Sample.
     * @param observer
     * */
    public void create(Smart4HealthWeeklyReportRecord record, Observer<Long> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(dao.insert(record)));
    }

    /** Selects the last week present in database.
     * @param observer
     * */
    public void selectLastWeekUploaded(Observer<Smart4HealthWeeklyReportRecord> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(dao.selectLastWeekUploaded()));
    }

    /** Inserts or updates the last week present in database.
     * @param year Year of uploading.
     * @param week Week of uploading.
     * @param value Boolean that define whether the FHIR resource was uploaded.
     * */
    public void createOrUpdateFhir(Integer year, Integer week, Boolean value) {
        CitizenHubDatabase.executorService().execute(() -> dao.insertOrReplaceFhir(year, week, value));
    }

    /** Selects the last week present in database.
     * @param year Year of uploading.
     * @param week Week of uploading.
     * @param value Boolean that define whether the PDF resource was uploaded.
     * */
    public void createOrUpdatePdf(Integer year, Integer week, Boolean value) {
        CitizenHubDatabase.executorService().execute(() -> dao.insertOrReplacePdf(year, week, value));
    }
}
