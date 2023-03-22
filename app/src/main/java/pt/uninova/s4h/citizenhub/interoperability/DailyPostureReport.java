package pt.uninova.s4h.citizenhub.interoperability;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DailyPostureReport {

    public static DailyPostureReport generateRandom(String patientId, LocalDate localDate) {
        final Instant startTime = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        final Instant endTime = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusSeconds(1).toInstant();

        final int[] hourlyGoodPosture = new int[24];
        final int[] hourlyBadPosture = new int[24];

        final Random random = new Random();
        random.setSeed(localDate.toEpochDay());

        for (int i = 0; i < 24; i++) {
            final int good = random.nextInt(3601);
            final int bad = random.nextInt(3601 - good);

            hourlyGoodPosture[i] = good;
            hourlyBadPosture[i] = bad;
        }

        return new DailyPostureReport(patientId, startTime, endTime, hourlyGoodPosture, hourlyBadPosture);
    }

    public static List<DailyPostureReport> generateRandom(String patientId, LocalDate firstDate, int numberOfReports) {
        final List<DailyPostureReport> reports = new ArrayList<>(numberOfReports);

        for (int i = 0; i < numberOfReports; i++) {
            reports.add(generateRandom(patientId, firstDate.plusDays(i)));
        }

        return reports;
    }

    private final String patientId;

    private final Instant startTime;
    private final Instant endTime;

    private final int[] hourlyBadPosture;
    private final int[] hourlyGoodPosture;

    public DailyPostureReport(String patientId, LocalDate day, int[] hourlyGoodPosture, int[] hourlyBadPosture) {
        this(patientId, day.atStartOfDay(ZoneId.systemDefault()).toInstant(), day.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusSeconds(1).toInstant(), hourlyGoodPosture, hourlyBadPosture);
    }

    public DailyPostureReport(String patientId, Instant startTime, Instant endTime, int[] hourlyGoodPosture, int[] hourlyBadPosture) {
        this.patientId = patientId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.hourlyGoodPosture = hourlyGoodPosture;
        this.hourlyBadPosture = hourlyBadPosture;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public int[] getHourlyBadPosture() {
        return hourlyBadPosture;
    }

    public int[] getHourlyGoodPosture() {
        return hourlyGoodPosture;
    }

    public String getPatientId() {
        return patientId;
    }

    public Instant getStartTime() {
        return startTime;
    }
}