package pt.uninova.s4h.citizenhub.persistence.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

import java.time.LocalDate;
import java.util.List;

import pt.uninova.s4h.citizenhub.persistence.conversion.EpochTypeConverter;
import pt.uninova.s4h.citizenhub.persistence.entity.Smart4HealthMonthlyReportRecord;

@Dao
public interface Smart4HealthMonthlyReportDao {

    @Insert
    long insert(Smart4HealthMonthlyReportRecord record);

    @Query("INSERT OR REPLACE INTO smart4health_monthly_report (year, month, fhir) VALUES (:year, :month, :value);")
    long insertOrReplaceFhir(Integer year, Integer month, Boolean value);

    @Query("INSERT OR REPLACE INTO smart4health_monthly_report (year, month, pdf) VALUES (:year, :month, :value);")
    long insertOrReplacePdf(Integer year, Integer month, Boolean value);

    @Query("SELECT * FROM smart4health_monthly_report ORDER BY year DESC, month DESC LIMIT 1;")
    @TypeConverters(EpochTypeConverter.class)
    Smart4HealthMonthlyReportRecord selectLastMonthUploaded();

    @Query("SELECT DISTINCT (timestamp / 86400000) * 86400000 FROM sample EXCEPT SELECT date FROM smart4health_daily_report;")
    @TypeConverters(EpochTypeConverter.class)
    List<LocalDate> selectMonthsWithValues();

}