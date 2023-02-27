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

    /** Verifies if there is a smart bear account logged in.
     * @return True if logged in, false if not. */
    public boolean hasSmartBearAccount() {
        return preferences.getBoolean("accounts.smartbear.enabled", false);
    }

    /** Disables smart bear account. */
    public void disableSmartBearAccount() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove("accounts.smartbear.id");
        editor.putBoolean("accounts.smartbear.enabled", false);

        editor.apply();
    }

    /** Enables smart bear account. */
    public void enableSmartBearAccount(int id) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("accounts.smartbear.enabled", true);
        editor.putInt("accounts.smartbear.id", id);

        editor.apply();

    }

    /** Gets smart bear id.
     * @return Id. */
    public int getSmartBearId() {
        return preferences.getInt("accounts.smartbear.id", -1);
    }

    /** Sets s,art bear account id.
     * @param value Id. */
    public void setSmartBearId(int value) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("accounts.smartbear.id", value);

        editor.apply();
    }

    /** Verifies if there is a smart 4 health account logged in.
     * @return Returns true if logged in. */
    public boolean hasSmart4HealthAccount() {
        return preferences.getBoolean("accounts.smart4health.enabled", true);
    }

    /** Disables smart 4 health account. */
    public void disableSmart4HealthAccount() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove("accounts.smart4health.id");
        editor.putBoolean("accounts.smart4health.enabled", false);
        editor.apply();
    }

    /** Enable smart 4 health account. */
    public void enableSmartHealthAccount() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("accounts.smart4health.enabled",true);

        editor.apply();
    }

    /** Sets automatic report upload on and off. */
    public void setReportAutomaticUpload(boolean automaticUploads){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("account.smart4health.report.auto-upload", automaticUploads);
        editor.apply();
    }

    /** Verifies if automatic report uploads are turned on or off.
     * @return Returns true if uploads are turned on and false if not. */
    public boolean hasReportAutomaticUpload(){
        return preferences.getBoolean("account.smart4health.report.auto-upload", true);
    }

    /** Sets weekly automatic report upload on and off.
     * @param weeklyAutomaticUpload True if weekly automatic upload are going to be turned on otherwise false. */
    public void setReportWeeklyAutomaticUpload(boolean weeklyAutomaticUpload){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("account.smart4health.report.weekly-auto-upload", weeklyAutomaticUpload);
        editor.apply();
    }

    /** Verifies if weekly automatic report uploads are turned on or off.
     * @return Returns true if uploads are turned on and false if not. */
    public boolean hasReportWeeklyAutomaticUpload(){
        return preferences.getBoolean("account.smart4health.report.weekly-auto-upload", true);
    }

    /** Sets monthly automatic report upload on and off.
     * @param weeklyAutomaticUpload True if monthly automatic upload are going to be turned on otherwise false. */
    public void setReportMonthlyAutomaticUpload(boolean weeklyAutomaticUpload){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("account.smart4health.report.monthly-auto-upload", weeklyAutomaticUpload);
        editor.apply();
    }

    /** Verifies if monthly automatic report uploads are turned on or off.
     * @return Returns true if uploads are turned on and false if not. */
    public boolean hasReportMonthlyAutomaticUpload(){
        return preferences.getBoolean("account.smart4health.report.monthly-auto-upload", true);
    }

    /** Sets if activity information will be displayed in the pdf report.
     * @param activity True if activity is going to appear otherwise false. */
    public void setReportDataActivity(boolean activity){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("account.smart4health.report.data.activity", activity);
        editor.apply();
    }

    /** Verifies if there is activity to be added to the report.
     * @return True if there is information and false if not. */
    public boolean hasReportDataActivity(){
        return preferences.getBoolean("account.smart4health.report.data.activity", true);
    }

    /** Sets if activity information will be displayed in the pdf report.
     * @param bloodPressure True if activity is going to appear otherwise false. */
    public void setReportDataBloodPressure(boolean bloodPressure){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("account.smart4health.report.data.blood-pressure", bloodPressure);
        editor.apply();
    }

    /** Verifies if there is blood pressure data to be added to the report.
     * @return True if there is information and false if not. */
    public boolean hasReportDataBloodPressure(){
        return preferences.getBoolean("account.smart4health.report.data.blood-pressure", true);
    }

    /** Sets if activity information will be displayed in the pdf report.
     * @param heartRate True if heart rate is going to appear otherwise false. */
    public void setReportDataHeartRate(boolean heartRate){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("account.smart4health.report.data.heart-rate", heartRate);
        editor.apply();
    }

    /** Verifies if there is heart rate data to be added to the report.
     * @return True if there is information and false if not. */
    public boolean hasReportDataHeartRate(){
        return preferences.getBoolean("account.smart4health.report.data.heart-rate", true);
    }

    /** Sets if activity information will be displayed in the pdf report.
     * @param lumbarExtensionTraining True if lumbar extension training is going to appear otherwise false. */
    public void setReportDataLumbarExtensionTraining(boolean lumbarExtensionTraining){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("account.smart4health.report.data.lumbar-extension-training", lumbarExtensionTraining);
        editor.apply();
    }

    /** Verifies if there is lumbar extension training data to be added to the report.
     * @return True if there is information and false if not. */
    public boolean hasReportDataLumbarExtensionTraining(){
        return preferences.getBoolean("account.smart4health.report.data.lumbar-extension-training", true);
    }

    /** Sets if posture information will be displayed in the pdf report.
     * @param posture True if posture is going to appear otherwise false. */
    public void setReportDataPosture(boolean posture){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("account.smart4health.report.data.posture", posture);
        editor.apply();
    }

    /** Verifies if there is posture data to be added to the report.
     * @return True if there is information and false if not. */
    public boolean hasReportDataPosture(){
        return preferences.getBoolean("account.smart4health.report.data.posture", true);
    }

}