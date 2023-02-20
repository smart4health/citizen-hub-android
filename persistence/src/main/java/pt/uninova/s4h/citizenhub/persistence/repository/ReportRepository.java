package pt.uninova.s4h.citizenhub.persistence.repository;

import android.content.Context;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.data.Tag;
import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.dao.ReportDao;
import pt.uninova.s4h.citizenhub.persistence.entity.util.BloodPressureSample;
import pt.uninova.s4h.citizenhub.persistence.entity.util.LumbarExtensionTrainingSample;
import pt.uninova.s4h.citizenhub.persistence.entity.util.ReportUtil;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

/** Repository used to call queries from the report dao. */
public class ReportRepository {

    private final ReportDao reportDao;

    public ReportRepository(Context context) {
        final CitizenHubDatabase citizenHubDatabase = CitizenHubDatabase.getInstance(context);
        reportDao = citizenHubDatabase.reportDao();
    }

    /** Selects work time daily simple records for the report.
     * @param localDate Date.
     * @param observer
     * */
    public void getWorkTimeSimpleRecords(LocalDate localDate, Observer<ReportUtil> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getSummaryTagged(localDate, localDate.plusDays(1), Tag.LABEL_CONTEXT_WORK)));
    }

    /** Selects not work time daily simple records for the report.
     * @param localDate Date.
     * @param observer
     * */
    public void getNotWorkTimeSimpleRecords(LocalDate localDate, Observer<ReportUtil> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getSummaryNotTagged(localDate, localDate.plusDays(1), Tag.LABEL_CONTEXT_WORK)));
    }

    /** Selects work time simple records for the weekly or monthly report.
     * @param localDate Date.
     * @param observer
     * */
    public void getWeeklyOrMonthlyWorkTimeSimpleRecords(LocalDate localDate, int days, Observer<ReportUtil> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getWeeklyOrMonthlyWorkTimeSimpleRecords(localDate.minusDays(days), localDate.plusDays(1), days)));
    }

    /** Selects not work time simple records for the weekly or monthly report.
     * @param localDate Date.
     * @param observer
     * */
    public void getWeeklyOrMonthlyNotWorkTimeSimpleRecords(LocalDate localDate, int days, Observer<ReportUtil> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getWeeklyOrMonthlyNotWorkTimeSimpleRecords(localDate.minusDays(days), localDate.plusDays(1), days)));
    }

    /** Selects work time blood pressure records for the daily report.
     * @param localDate Date.
     * @param observer
     * */
    public void getWorkTimeBloodPressure(LocalDate localDate, Observer<List<BloodPressureSample>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getWorkTimeBloodPressure(localDate, localDate.plusDays(1))));
    }

    /** Selects not work time blood pressure records for the daily report.
     * @param localDate Date.
     * @param observer
     * */
    public void getNotWorkTimeBloodPressure(LocalDate localDate, Observer<List<BloodPressureSample>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getNotWorkTimeBloodPressure(localDate, localDate.plusDays(1))));
    }

    /** Selects not work time lumbar extension records for the daily report.
     * @param localDate Date.
     * @param observer
     * */
    public void getWorkTimeLumbarExtensionTraining(LocalDate localDate, Observer<List<LumbarExtensionTrainingSample>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getWorkTimeLumbarExtensionTraining(localDate, localDate.plusDays(1))));
    }

    /** Selects not work time lumbar extension records for the daily report.
     * @param localDate Date.
     * @param observer
     * */
    public void getNotWorkTimeLumbarExtensionTraining(LocalDate localDate, Observer<List<LumbarExtensionTrainingSample>> observer) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getNotWorkTimeLumbarExtensionTraining(localDate, localDate.plusDays(1))));
    }

    public void getSampleId(Observer<List<ReportUtil>> observer, LocalDate localDate) {
        CitizenHubDatabase.executorService().execute(() -> observer.observe(reportDao.getSampleID(localDate, localDate.plusDays(1))));
    }

}
