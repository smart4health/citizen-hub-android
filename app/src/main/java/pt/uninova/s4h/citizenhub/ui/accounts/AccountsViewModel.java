package pt.uninova.s4h.citizenhub.ui.accounts;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.preference.PreferenceManager;

public class AccountsViewModel extends AndroidViewModel {

    private final SharedPreferences preferences;

    public AccountsViewModel(@NonNull Application application) {
        super(application);

        preferences = PreferenceManager.getDefaultSharedPreferences(application.getApplicationContext());
    }

    public void disableSmartBearAccount() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove("accounts.smartbear.id");
        editor.putBoolean("accounts.smartbear.enabled", false);

        editor.apply();
    }

    public void enableSmartBearAccount(int id) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("accounts.smartbear.enabled", true);
        editor.putInt("accounts.smartbear.id", id);

        editor.apply();

    }

    public void enableSmartHealthAccount(){
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("accounts.smart4health.enabled",true);

        editor.apply();
    }

    public int getSmartBearId() {
        return preferences.getInt("accounts.smartbear.id", -1);
    }

    public boolean hasSmart4HealthAccount() {
        return preferences.getBoolean("accounts.smart4health.enabled", true);
    }

    public void setReportAutomaticUpload(boolean automaticUploads){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("account.smart4health.report.auto-upload", automaticUploads);
        editor.apply();
    }

    public boolean hasReportAutomaticUpload(){
        return preferences.getBoolean("account.smart4health.report.auto-upload", true);
    }

    public void setReportWeeklyAutomaticUpload(boolean weeklyAutomaticUpload){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("account.smart4health.report.weekly-auto-upload", weeklyAutomaticUpload);
        editor.apply();
    }

    public boolean hasReportWeeklyAutomaticUpload(){
        return preferences.getBoolean("account.smart4health.report.weekly-auto-upload", true);
    }

    public void setReportMonthlyAutomaticUpload(boolean weeklyAutomaticUpload){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("account.smart4health.report.monthly-auto-upload", weeklyAutomaticUpload);
        editor.apply();
    }

    public boolean hasReportMonthlyAutomaticUpload(){
        return preferences.getBoolean("account.smart4health.report.monthly-auto-upload", true);
    }

    public void setReportDataActivity(boolean activity){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("account.smart4health.report.data.activity", activity);
        editor.apply();
    }

    public boolean hasReportDataActivity(){
        return preferences.getBoolean("account.smart4health.report.data.activity", true);
    }

    public void setReportDataBloodPressure(boolean bloodPressure){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("account.smart4health.report.data.blood-pressure", bloodPressure);
        editor.apply();
    }

    public boolean hasReportDataBloodPressure(){
        return preferences.getBoolean("account.smart4health.report.data.blood-pressure", true);
    }

    public void setReportDataHeartRate(boolean heartRate){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("account.smart4health.report.data.heart-rate", heartRate);
        editor.apply();
    }

    public boolean hasReportDataHeartRate(){
        return preferences.getBoolean("account.smart4health.report.data.heart-rate", true);
    }

    public void setReportDataLumbarExtensionTraining(boolean lumbarExtensionTraining){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("account.smart4health.report.data.lumbar-extension-training", lumbarExtensionTraining);
        editor.apply();
    }

    public boolean hasReportDataLumbarExtensionTraining(){
        return preferences.getBoolean("account.smart4health.report.data.lumbar-extension-training", true);
    }

    public void setReportDataPosture(boolean posture){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("account.smart4health.report.data.posture", posture);
        editor.apply();
    }

    public boolean hasReportDataPosture(){
        return preferences.getBoolean("account.smart4health.report.data.posture", true);
    }

    public boolean hasSmartBearAccount() {
        return preferences.getBoolean("accounts.smartbear.enabled", false);
    }

    public void setSmartBearId(int value) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("accounts.smartbear.id", value);

        editor.apply();
    }

    public void disableSmart4HealthAccount() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove("accounts.smart4health.id");
        editor.putBoolean("accounts.smart4health.enabled", false);
        editor.apply();
    }
}