package pt.uninova.s4h.citizenhub.persistence.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "smart4health_monthly_report", primaryKeys = {"year", "month"})
public class Smart4HealthMonthlyReportRecord {

    @NonNull
    @ColumnInfo(defaultValue = "0")
    private Boolean fhir;

    @NonNull
    @ColumnInfo(defaultValue = "0")
    private Boolean pdf;

    @NonNull
    private Integer month;

    @NonNull
    private Integer year;

    public Smart4HealthMonthlyReportRecord(Integer year, Integer month, Boolean fhir, Boolean pdf) {
        this.year = year;
        this.month = month;
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

    public Integer getMonth() {
        return month;
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

    public void setMonth(Integer month) {
        this.month = month;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
