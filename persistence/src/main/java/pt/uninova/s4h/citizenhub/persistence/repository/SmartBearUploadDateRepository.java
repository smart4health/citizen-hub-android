package pt.uninova.s4h.citizenhub.persistence;

import android.content.Context;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class SmartBearUploadDateRepository {

    private final SmartBearUploadDateDao dao;

    public SmartBearUploadDateRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);

        dao = citizenHubDatabase.smartBearUploadDateDao();
    }

    public void add(SmartBearUploadDateRecord record) {
        CitizenHubDatabase.executorService().execute(() -> {
            dao.insert(record);
        });
    }

    public void obtainDaysWithData(Observer<List<LocalDate>> observer) {
        CitizenHubDatabase.executorService().execute(() -> {
            final List<LocalDate> localDates = dao.selectDaysWithValues();

            observer.observe(localDates);
        });
    }

}
