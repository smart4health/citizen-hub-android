package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.Smart4HealthMonthlyReportDao;
import pt.uninova.s4h.citizenhub.persistence.entity.Smart4HealthMonthlyReportRecord;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Repository used to call queries from the smart4health monthly report dao. */
public class Smart4HealthMonthlyReportRepository {

    private final Smart4HealthMonthlyReportDao dao;

    public Smart4HealthMonthlyReportRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        dao = citizenHubDatabase.smart4HealthMonthlyReportDao();
    }

    /** Inserts a new record into the database.
     * @param record Sample.
     * */
    public void create(Smart4HealthMonthlyReportRecord record) {
        CitizenHubDatabase.executorService().execute(() -> dao.insert(record));
    }

    /** Inserts a new record into the database.
     * @param record Sample.
     * @param observer
     * */
    public void create(Smart4HealthMonthlyReportRecord record, Observer<Long> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(dao.insert(record)));
    }

    /** Selects the last month present in database.
     * @param observer
     * */
    public void selectLastMonthUploaded(Observer<Smart4HealthMonthlyReportRecord> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(dao.selectLastMonthUploaded()));
    }

    /** Inserts or updates the last month present in database.
     * @param year Year of uploading.
     * @param month Month of uploading.
     * @param value Boolean that define whether the FHIR resource was uploaded.
     * */
    public void createOrUpdateFhir(Integer year, Integer month, Boolean value) {
        CitizenHubDatabase.executorService().execute(() -> dao.insertOrReplaceFhir(year, month, value));
    }

    /** Selects the last month present in database.
     * @param year Year of uploading.
     * @param month Month of uploading.
     * @param value Boolean that define whether the PDF resource was uploaded.
     * */
    public void createOrUpdatePdf(Integer year, Integer month, Boolean value) {
        CitizenHubDatabase.executorService().execute(() -> dao.insertOrReplacePdf(year, month, value));
    }

}
