package pt.uninova.s4h.citizenhub.work;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.preference.PreferenceManager;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import java.time.LocalDate;

import pt.uninova.s4h.citizenhub.BuildConfig;
import pt.uninova.s4h.citizenhub.interoperability.DailyPostureReport;
import pt.uninova.s4h.citizenhub.interoperability.SmartBearClient;
import pt.uninova.s4h.citizenhub.persistence.entity.util.HourlyPosture;
import pt.uninova.s4h.citizenhub.persistence.repository.PostureMeasurementRepository;
import pt.uninova.s4h.citizenhub.persistence.repository.SmartBearDailyReportRepository;

public class SmartBearUploader extends ListenableWorker {

    public SmartBearUploader(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            final boolean enabled = preferences.getBoolean("accounts.smartbear.enabled", false);

            if (enabled) {
                final int patientId = preferences.getInt("accounts.smartbear.id", -1);

                final PostureMeasurementRepository postureMeasurementRepository = new PostureMeasurementRepository(getApplicationContext());
                final SmartBearDailyReportRepository smartBearDailyReportRepository = new SmartBearDailyReportRepository(getApplicationContext());

                final SmartBearClient client = new SmartBearClient(BuildConfig.SMART_BEAR_URL, BuildConfig.SMART_BEAR_API_KEY);

                final LocalDate now = LocalDate.now();

                smartBearDailyReportRepository.readDaysWithData(value -> {
                    for (LocalDate i : value) {
                        if (i.compareTo(now) < 0) {
                            postureMeasurementRepository.read(i, hourlyPostures -> {
                                int[] good = new int[24];
                                int[] bad = new int[24];

                                for (HourlyPosture j : hourlyPostures) {
                                    good[j.getHour()] = (int) j.getCorrectPostureDuration().getSeconds();
                                    bad[j.getHour()] = (int) j.getIncorrectPostureDuration().getSeconds();
                                }

                                DailyPostureReport dailyPostureReport = new DailyPostureReport(Integer.toString(patientId), i, good, bad);

                                client.upload(dailyPostureReport, response -> {
                                    if (response.isSuccessful()) {
                                        smartBearDailyReportRepository.createUTC(i);
                                    }
                                });

                            });
                        }
                    }

                    completer.set(Result.success());
                });
            } else {
                completer.set(Result.success());
            }

            return this.toString();
        });
    }
}