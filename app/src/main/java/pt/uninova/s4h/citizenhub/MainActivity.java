package pt.uninova.s4h.citizenhub;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import care.data4life.sdk.Data4LifeClient;
import care.data4life.sdk.lang.D4LException;
import care.data4life.sdk.listener.ResultListener;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.BluetoothConnectionState;
import pt.uninova.s4h.citizenhub.ui.devices.DeviceViewModel;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        NavController navController = ((NavHostFragment) this.getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment)).getNavController();
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setItemIconTintList(null);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        appBarConfiguration =
                new AppBarConfiguration.Builder(R.id.summary_fragment, R.id.report_master_fragment, R.id.lumbar_extension_training_fragment, R.id.device_list_fragment, R.id.settings_fragment, R.id.smart4health_fragment, R.id.accounts_fragment, R.id.about_fragment)
                        .setOpenableLayout(drawerLayout)
                        .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final String bugreport = preferences.getString("bugreport", null);

        if (bugreport != null) {
            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.bugreport_message))
                    .setPositiveButton(R.string.bugreport_positive_button, (dialog, which) -> {
                        Thread t = new Thread(() -> {
                            Looper.prepare();

                            OkHttpClient client = new OkHttpClient();

                            RequestBody requestBody = RequestBody.create(bugreport, MediaType.get("text/plain"));
                            Request request = new Request.Builder()
                                    .url("https://bugreport.smart4health.grisenergia.pt/stacktrace")
                                    .post(requestBody)
                                    .build();

                            try (final Response response = client.newCall(request).execute()) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), R.string.bugreport_confirmation, Toast.LENGTH_SHORT).show();
                                    preferences.edit().remove("bugreport").apply();
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        });

                        t.start();

                    })
                    .setNegativeButton(R.string.bugreport_negative_button, (dialog, which) -> {
                        preferences.edit().remove("bugreport").apply();
                    })
                    .create();

            alertDialog.show();
        }

        final Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            final StringWriter stringWriter = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stringWriter);

            e.printStackTrace(printWriter);

            final SharedPreferences.Editor editor = preferences.edit();

            editor.putString("bugreport", stringWriter.toString());
            editor.commit();

            if (uncaughtExceptionHandler != null)
                uncaughtExceptionHandler.uncaughtException(t, e);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        final NavigationView navView = findViewById(R.id.nav_view);
        final Menu nav_Menu = navView.getMenu();

        Data4LifeClient client = Data4LifeClient.getInstance();

        client.isUserLoggedIn(new ResultListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                runOnUiThread(() -> nav_Menu.findItem(R.id.smart4health_fragment).setVisible(aBoolean));
            }

            @Override
            public void onError(@NonNull D4LException e) {
                runOnUiThread(() -> nav_Menu.findItem(R.id.smart4health_fragment).setVisible(false));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = ((NavHostFragment) this.getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment)).getNavController();

        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        final DeviceViewModel model = new ViewModelProvider(MainActivity.this).get(DeviceViewModel.class);
        model.getDeviceConnection().disconnect();
        model.getDeviceConnection().close();

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (model.getDeviceConnection().getConnectionState() != BluetoothConnectionState.DISCONNECTED.ordinal()) {

                    model.getDeviceConnection().disconnect();
                    model.getDeviceConnection().close();
                    if (model.getSelectedDeviceAgent() != null) {

                        model.getSelectedDeviceAgent().disable();
                        model.removeSelectedDevice();
                    }
                }
            }
        }, 10000);


        NavController navController = ((NavHostFragment) this.getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment)).getNavController();

        if (navController.getCurrentBackStackEntry().getDestination().getId() == R.id.summary_fragment) {
            moveTaskToBack(false);
        } else {
            navController.popBackStack();
        }
    }

}