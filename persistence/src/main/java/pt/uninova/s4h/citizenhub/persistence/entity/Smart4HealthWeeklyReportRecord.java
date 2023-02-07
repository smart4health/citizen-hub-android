package pt.uninova.s4h.citizenhub.persistence.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "smart4health_weekly_report", primaryKeys = {"year", "week"})
public class Smart4HealthWeeklyReportRecord {

    @NonNull
    @ColumnInfo(defaultValue = "0")
    private Boolean fhir;

    @NonNull
    @ColumnInfo(defaultValue = "0")
    private Boolean pdf;

    @NonNull
    private Integer week;

    @NonNull
    private Integer year;

    public Smart4HealthWeeklyReportRecord(Integer year, Integer week, Boolean fhir, Boolean pdf) {
        this.year = year;
        this.week = week;
        this.fhir = fhir;
        this.pdf = pdf;
    }

    @NonNull
    public Boolean getFhir() {
        return fhir;
    }

    @NonNull
    public Boolean getPdf() {
        return pdf;
    }

    public Integer getWeek() {
        return week;
    }

    public Integer getYear() {
        return year;
    }

    public void setFhir(Boolean fhir) {
        this.fhir = fhir;
    }

    public void setPdf(Boolean pdf) {
        this.pdf = pdf;
    }

    public void setWeek(Integer week) {
        this.week = week;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
