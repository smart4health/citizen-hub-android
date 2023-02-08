package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.data.Tag;
import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.ReportDao;
import pt.uninova.s4h.citizenhub.persistence.entity.util.ReportUtil;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class ReportRepository {

    private final ReportDao reportDao;

    public ReportRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);
        reportDao = citizenHubDatabase.reportDao();
    }

    public void getSimpleDailyRecords(LocalDate localDate, Observer<List<ReportUtil>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getSimpleDailyRecords(localDate, localDate.plusDays(1))));
    }

    public void getWorkTimeSimpleRecords(LocalDate localDate, Observer<ReportUtil> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getSummaryTagged(localDate, localDate.plusDays(1), Tag.LABEL_CONTEXT_WORK)));
    }

    public void getNotWorkTimeSimpleRecords(LocalDate localDate, Observer<ReportUtil> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getSummaryNotTagged(localDate, localDate.plusDays(1), Tag.LABEL_CONTEXT_WORK)));
    }

    public void getWeeklyOrMonthlyWorkTimeSimpleRecords(LocalDate localDate, int days, Observer<ReportUtil> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getWeeklyOrMonthlyWorkTimeSimpleRecords(localDate.minusDays(days), localDate.plusDays(1), days)));
    }

    public void getWeeklyOrMonthlyNotWorkTimeSimpleRecords(LocalDate localDate, int days, Observer<ReportUtil> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getWeeklyOrMonthlyNotWorkTimeSimpleRecords(localDate.minusDays(days), localDate.plusDays(1), days)));
    }

    public void getBloodPressure(LocalDate localDate, Observer<List<ReportUtil>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getBloodPressure(localDate, localDate.plusDays(1))));
    }

    public void getWorkTimeBloodPressure(LocalDate localDate, Observer<List<ReportUtil>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getWorkTimeBloodPressure(localDate, localDate.plusDays(1))));
    }

    public void getNotWorkTimeBloodPressure(LocalDate localDate, Observer<List<ReportUtil>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getNotWorkTimeBloodPressure(localDate, localDate.plusDays(1))));
    }

    public void getCalories(Observer<List<ReportUtil>> observer, LocalDate localDate) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getCalories(localDate, localDate.plusDays(1))));
    }

    public void getDistance(Observer<List<ReportUtil>> observer, LocalDate localDate) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getDistance(localDate, localDate.plusDays(1))));
    }

    public void getHeartRate(Observer<List<ReportUtil>> observer, LocalDate localDate) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getHeartRate(localDate, localDate.plusDays(1))));
    }

    public void getLumbarExtensionTraining(LocalDate localDate, Observer<List<ReportUtil>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getLumbarExtensionTraining(localDate, localDate.plusDays(1))));
    }

    public void getWorkTimeLumbarExtensionTraining(LocalDate localDate, Observer<List<ReportUtil>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getWorkTimeLumbarExtensionTraining(localDate, localDate.plusDays(1))));
    }

    public void getNotWorkTimeLumbarExtensionTraining(LocalDate localDate, Observer<List<ReportUtil>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getNotWorkTimeLumbarExtensionTraining(localDate, localDate.plusDays(1))));
    }

    public void getPosture(Observer<List<ReportUtil>> observer, LocalDate localDate) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getPosture(localDate, localDate.plusDays(1))));
    }

    public void getSteps(Observer<List<ReportUtil>> observer, LocalDate localDate) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getSteps(localDate, localDate.plusDays(1))));
    }

    public void getSampleId(Observer<List<ReportUtil>> observer, LocalDate localDate) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getSampleID(localDate, localDate.plusDays(1))));
    }

}
