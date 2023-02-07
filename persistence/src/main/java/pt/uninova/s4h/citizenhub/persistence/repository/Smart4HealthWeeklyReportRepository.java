package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.Smart4HealthWeeklyReportDao;
import pt.uninova.s4h.citizenhub.persistence.entity.Smart4HealthWeeklyReportRecord;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class Smart4HealthWeeklyReportRepository {

    private final Smart4HealthWeeklyReportDao dao;

    public Smart4HealthWeeklyReportRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        dao = citizenHubDatabase.smart4HealthWeeklyReportDao();
    }

    public void create(Smart4HealthWeeklyReportRecord record) {
        CitizenHubDatabase.executorService().execute(() -> dao.insert(record));
    }

    public void create(Smart4HealthWeeklyReportRecord record, Observer<Long> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(dao.insert(record)));
    }

    public void selectLastWeekUploaded(Observer<Smart4HealthWeeklyReportRecord> observer){
        CitizenHubDatabase.executorService().execute(() -> observer.observe(dao.selectLastWeekUploaded()));
    }

    public void createOrUpdateFhir(Integer year, Integer week, Boolean value) {
        CitizenHubDatabase.executorService().execute(() -> dao.insertOrReplaceFhir(year, week, value));
    }

    public void createOrUpdatePdf(Integer year, Integer week, Boolean value) {
        CitizenHubDatabase.executorService().execute(() -> dao.insertOrReplacePdf(year, week, value));
    }
}
