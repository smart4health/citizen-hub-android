package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.Smart4HealthMonthlyReportDao;
import pt.uninova.s4h.citizenhub.persistence.entity.Smart4HealthMonthlyReportRecord;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class Smart4HealthMonthlyReportRepository {

    private final Smart4HealthMonthlyReportDao dao;

    public Smart4HealthMonthlyReportRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        dao = citizenHubDatabase.smart4HealthMonthlyReportDao();
    }

    public void create(Smart4HealthMonthlyReportRecord record) {
        CitizenHubDatabase.executorService().execute(() -> dao.insert(record));
    }

    public void create(Smart4HealthMonthlyReportRecord record, Observer<Long> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(dao.insert(record)));
    }

    public void selectLastMonthUploaded(Observer<Smart4HealthMonthlyReportRecord> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(dao.selectLastMonthUploaded()));
    }

    public void createOrUpdateFhir(Integer year, Integer month, Boolean value) {
        CitizenHubDatabase.executorService().execute(() -> dao.insertOrReplaceFhir(year, month, value));
    }

    public void createOrUpdatePdf(Integer year, Integer week, Boolean value) {
        CitizenHubDatabase.executorService().execute(() -> dao.insertOrReplacePdf(year, week, value));
    }

}
