package pt.uninova.s4h.citizenhub;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import pt.uninova.s4h.citizenhub.data.HeartRateMeasurement;
import pt.uninova.s4h.citizenhub.data.StepsSnapshotMeasurement;

public class SettingsFragment extends Fragment {

    private Switch switchHeartRate, switchSteps;
    private TextView textPhoneConnected;
    private CompoundButton.OnCheckedChangeListener heartRateListener, stepsListener;
    private final String citizenHubPath = "/citizenhub_";
    SharedPreferences sharedPreferences;
    private final String sharedPreferencesVariableHeartRateBoolean = "HeartRate";
    private final String sharedPreferencesVariableStepsBoolean = "Steps";

    private View heartRateControl;
    private View stepsControl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        switchHeartRate = view.findViewById(R.id.switchHeartRate);
        switchSteps = view.findViewById(R.id.switchSteps);
        heartRateControl = view.findViewById(R.id.heartRateControl);
        stepsControl = view.findViewById(R.id.stepsControl);
        textPhoneConnected = view.findViewById(R.id.textInfoPhone);

        enableObservers(view);

        heartRateListener = (compoundButton, isChecked) -> {
            MainActivity.protocolHeartRate.setValue(isChecked);
            sharedPreferences.edit().putBoolean(sharedPreferencesVariableHeartRateBoolean, isChecked).apply();
            Date now = new Date();
            String msg = checkedToCommunicationValue(isChecked) + "," + now.getTime() + "," + HeartRateMeasurement.TYPE_HEART_RATE;
            String dataPath = citizenHubPath + MainActivity.nodeIdString;
            new SendMessage(dataPath, msg).start();
        };

        stepsListener = (compoundButton, isChecked) -> {
            MainActivity.protocolSteps.setValue(isChecked);
            sharedPreferences.edit().putBoolean(sharedPreferencesVariableStepsBoolean, isChecked).apply();
            Date now = new Date();
            String msg = checkedToCommunicationValue(isChecked) + "," + now.getTime() + "," + StepsSnapshotMeasurement.TYPE_STEPS_SNAPSHOT;
            String dataPath = citizenHubPath + MainActivity.nodeIdString;
            new SendMessage(dataPath, msg).start();
        };

        enabledCheckedListeners(true);

        String sharedPreferencesName = "switchesSharedPreferences";
        sharedPreferences = requireActivity().getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
        MainActivity.protocolSteps.setValue(sharedPreferences.getBoolean(sharedPreferencesVariableStepsBoolean, false));
        MainActivity.protocolHeartRate.setValue(sharedPreferences.getBoolean(sharedPreferencesVariableHeartRateBoolean, false));


        heartRateControl.setVisibility(MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null ? View.VISIBLE : View.GONE);
        stepsControl.setVisibility(MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null ? View.VISIBLE : View.INVISIBLE);

        return view;
    }

    private void enableObservers(View view) {
        MainActivity.protocolPhoneConnected.observe((LifecycleOwner) view.getContext(), aBoolean -> {
            if (aBoolean) textPhoneConnected.setText(R.string.show_data_phone_connected);
            else textPhoneConnected.setText(R.string.show_data_phone_not_connected);
        });

        MainActivity.protocolSteps.observe((LifecycleOwner) view.getContext(), aBoolean -> {
            enabledCheckedListeners(false);
            if (aBoolean) {
                switchSteps.setChecked(true);
                enableStepsSensor(true);
            } else {
                switchSteps.setChecked(false);
                enableStepsSensor(false);
            }
            enabledCheckedListeners(true);
        });

        MainActivity.protocolHeartRate.observe((LifecycleOwner) view.getContext(), aBoolean -> {
            enabledCheckedListeners(false);
            if (aBoolean) {
                switchHeartRate.setChecked(true);
                enableHeartRateSensor(true);
            } else {
                switchHeartRate.setChecked(false);
                enableHeartRateSensor(false);
            }
            enabledCheckedListeners(true);
        });
    }

    private void enableHeartRateSensor(Boolean enabled) {
        if (MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null) {
            if (enabled)
                MainActivity.sensorManager.registerListener(MainActivity.heartRateListener, MainActivity.heartSensor, SensorManager.SENSOR_DELAY_NORMAL);
            else
                MainActivity.sensorManager.unregisterListener(MainActivity.heartRateListener, MainActivity.heartSensor);
        }
    }

    private void enableStepsSensor(Boolean enabled) {
        if (MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            if (enabled)
                MainActivity.sensorManager.registerListener(MainActivity.stepsListener, MainActivity.stepsCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
            else
                MainActivity.sensorManager.unregisterListener(MainActivity.stepsListener, MainActivity.stepsCounterSensor);
        }
    }

    private int checkedToCommunicationValue(boolean isChecked) {
        if (isChecked) return 100001;
        else return 100000;
    }

    private void enabledCheckedListeners(Boolean enabled) {
        if (enabled) {
            switchHeartRate.setOnCheckedChangeListener(heartRateListener);
            switchSteps.setOnCheckedChangeListener(stepsListener);
        } else {
            switchHeartRate.setOnCheckedChangeListener(null);
            switchSteps.setOnCheckedChangeListener(null);
        }
    }

    class SendMessage extends Thread {
        String path;
        String message;

        SendMessage(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {
            Task<List<Node>> nodeListTask = Wearable.getNodeClient(requireContext()).getConnectedNodes();
            try {
                List<Node> nodes = Tasks.await(nodeListTask);
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask = Wearable.getMessageClient(requireActivity()).sendMessage(node.getId(), path, message.getBytes());
                    try {
                        Tasks.await(sendMessageTask);
                    } catch (ExecutionException | InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }
            } catch (ExecutionException | InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }
}