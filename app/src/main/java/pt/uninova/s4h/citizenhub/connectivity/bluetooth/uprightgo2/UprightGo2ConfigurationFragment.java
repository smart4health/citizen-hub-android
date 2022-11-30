package pt.uninova.s4h.citizenhub.connectivity.bluetooth.uprightgo2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.connectivity.Agent;
import pt.uninova.s4h.citizenhub.connectivity.StateChangedMessage;
import pt.uninova.s4h.citizenhub.data.Device;
import pt.uninova.s4h.citizenhub.ui.devices.DeviceViewModel;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class UprightGo2ConfigurationFragment extends Fragment {

    public static String uprightGo2MenuItem = "calibration";

    private SwitchCompat postureCorrectionVibration;
    private LinearLayout buttonCalibration;
    private LinearLayout spinnerIntervalLayout;
    private LinearLayout spinnerPatternLayout;
    private LinearLayout spinnerStrengthLayout;

    protected ViewStub deviceAdvancedSettings;
    protected View deviceAdvancedSettingsInflated;
    private DeviceViewModel model;
    private boolean vibration;
    private int angle;
    private int interval;
    private int pattern;
    private boolean showPattern;
    private int strength;

    private final Observer<StateChangedMessage<Integer, ? extends Agent>> agentStateObserver = value -> {
        if (value.getNewState() == Agent.AGENT_STATE_ENABLED) {
            requireActivity().runOnUiThread(() -> {
                setView(buttonCalibration, vibration);
                setView(spinnerIntervalLayout, vibration);
                setView(spinnerStrengthLayout, vibration);
                setView(spinnerPatternLayout, vibration);
            });
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        model = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_device_configuration_advanced, container, false);

        deviceAdvancedSettings = view.findViewById(R.id.layoutStubConfigurationAdvancedSettings);
        deviceAdvancedSettings.setLayoutResource(R.layout.fragment_device_configuration_uprightgo2);
        deviceAdvancedSettingsInflated = deviceAdvancedSettings.inflate();

        buttonCalibration = deviceAdvancedSettingsInflated.findViewById(R.id.calibrationLayout);
        buttonCalibration.setOnClickListener(view1 -> Navigation.findNavController(requireView()).navigate(pt.uninova.s4h.citizenhub.ui.devices.DeviceConfigurationFragmentDirections.actionDeviceConfigurationStreamsFragmentToUprightGo2CalibrationFragment()));


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Device device = model.getSelectedDevice().getValue();

        spinnerIntervalLayout = view.findViewById(R.id.layoutVibrationInterval);
        spinnerPatternLayout = view.findViewById(R.id.layoutVibrationPattern);
        spinnerStrengthLayout = view.findViewById(R.id.layoutVibrationStrength);


        model.getSelectedDeviceAgent().getSettingsManager().get("First Time", new Observer<String>() {
            @Override
            public void observe(String value) {
                if (value == null) {
                    model.getSelectedDeviceAgent().getSettingsManager().set("posture-correction-vibration", "false");
                    model.getSelectedDeviceAgent().getSettingsManager().set("vibration-angle", "0");
                    model.getSelectedDeviceAgent().getSettingsManager().set("vibration-interval", "0");
                    model.getSelectedDeviceAgent().getSettingsManager().set("vibration-pattern", "0");
                    model.getSelectedDeviceAgent().getSettingsManager().set("show-vibration-pattern", "true");
                    model.getSelectedDeviceAgent().getSettingsManager().set("vibration-strength", "0");

                    model.getSelectedDeviceAgent().getSettingsManager().set("First Time", "1");
                    setupAdvancedConfigurationsUprightGo2(deviceAdvancedSettingsInflated, model, device);

                } else {
                    setupAdvancedConfigurationsUprightGo2(deviceAdvancedSettingsInflated, model, device);

                }
            }
        });
    }

    protected void setupAdvancedConfigurationsUprightGo2(View view, DeviceViewModel model, Device device) {
        //posture-correction-vibration ON/OFF
        postureCorrectionVibration = view.findViewById(R.id.switchPostureCorrection);
        if (model.getSelectedDeviceAgent().getState() != Agent.AGENT_STATE_ENABLED) {
            postureCorrectionVibration.setAlpha(0.5f);
        }

        model.getSelectedDeviceAgent().getSettingsManager().get("posture-correction-vibration", new Observer<String>() {
            @Override
            public void observe(String value) {
                if (value.equals("true")) {
                    vibration = true;
                    requireActivity().runOnUiThread(() -> {
                        postureCorrectionVibration.setChecked(true);
                        setView(buttonCalibration, true);
                        setView(spinnerIntervalLayout, true);
                        setView(spinnerStrengthLayout, true);
                        setView(spinnerPatternLayout, true);
                    });
                } else {
                    if (value.equals("false")) {
                        vibration = false;
                        requireActivity().runOnUiThread(() -> {
                            postureCorrectionVibration.setChecked(false);
                            setView(buttonCalibration, false);
                            setView(spinnerIntervalLayout, false);
                            setView(spinnerStrengthLayout, false);
                            setView(spinnerPatternLayout, false);
                        });
                    }
                }
            }
        });

        postureCorrectionVibration.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                model.getSelectedDeviceAgent().getSettingsManager().set("posture-correction-vibration", "true");
                vibration = true;
                setView(buttonCalibration, true);
                setView(spinnerIntervalLayout, true);
                setView(spinnerStrengthLayout, true);
                setView(spinnerPatternLayout, true);
            } else {
                model.getSelectedDeviceAgent().getSettingsManager().set("posture-correction-vibration", "false");
                vibration = false;
                setView(buttonCalibration, false);
                setView(spinnerIntervalLayout, false);
                setView(spinnerStrengthLayout, false);
                setView(spinnerPatternLayout, false);
            }
            setSetting(model.getSelectedDeviceAgent());
        });

        //vibration-angle (1 (strict) to 6 (relaxed))

        Spinner spinnerAngle = view.findViewById(R.id.spinnerVibrationAngle);

        model.getSelectedDeviceAgent().getSettingsManager().get("vibration-angle", new Observer<String>() {
            @Override
            public void observe(String value) {
                spinnerAngle.setSelection(Integer.parseInt(value));
                angle = Integer.parseInt(value);
            }
        });
        setSpinnerAlpha(spinnerAngle);
        spinnerAngle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getChildAt(0) != null) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(ContextCompat.getColor(requireContext(), R.color.colorS4HDarkBlue));
                }
                model.getSelectedDeviceAgent().getSettingsManager().set("vibration-angle", String.valueOf(spinnerAngle.getSelectedItemPosition()));
                angle = spinnerAngle.getSelectedItemPosition();

                setSetting(model.getSelectedDeviceAgent());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do a default thingy
            }
        });
        Spinner spinnerInterval = view.findViewById(R.id.spinnerVibrationInterval);

        model.getSelectedDeviceAgent().getSettingsManager().get("vibration-interval", new Observer<String>() {
            @Override
            public void observe(String value) {
                spinnerInterval.setSelection(Integer.parseInt(value));
                interval = Integer.parseInt(value);
            }
        });
        setSpinnerAlpha(spinnerInterval);
        spinnerInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getChildAt(0) != null)
                    ((TextView) adapterView.getChildAt(0)).setTextColor(ContextCompat.getColor(requireContext(), R.color.colorS4HDarkBlue));
                model.getSelectedDeviceAgent().getSettingsManager().set("vibration-interval", String.valueOf(spinnerInterval.getSelectedItemPosition()));
                interval = spinnerInterval.getSelectedItemPosition();
                setSetting(model.getSelectedDeviceAgent());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do a default thingy
            }
        });
        //vibration-pattern (0 (long), 1 (medium), 2 (short), 3 (ramp up), 4 (knock knock),
        // 5 (heartbeat), 6 (tuk tuk), 7 (ecstatic), 8 (muzzle))
        Spinner spinnerPattern = view.findViewById(R.id.spinnerVibrationPattern);

        model.getSelectedDeviceAgent().getSettingsManager().get("vibration-pattern", new Observer<String>() {
            @Override
            public void observe(String value) {
                spinnerPattern.setSelection(Integer.parseInt(value));
                pattern = Integer.parseInt(value);
            }
        });
        setSpinnerAlpha(spinnerPattern);
        spinnerPattern.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getChildAt(0) != null)
                    ((TextView) adapterView.getChildAt(0)).setTextColor(ContextCompat.getColor(requireContext(), R.color.colorS4HDarkBlue));

                model.getSelectedDeviceAgent().getSettingsManager().set("vibration-pattern", String.valueOf(spinnerPattern.getSelectedItemPosition()));
                pattern = spinnerPattern.getSelectedItemPosition();
                setSetting(model.getSelectedDeviceAgent());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do a default thingy
            }
        });

        Spinner correctionStrength = view.findViewById(R.id.spinnerVibrationStrength);

        model.getSelectedDeviceAgent().getSettingsManager().get("vibration-strength", new Observer<String>() {
            @Override
            public void observe(String value) {
                if (value == null) {
                    correctionStrength.setSelection(0);
                    strength = 0;
                } else {
                    correctionStrength.setSelection(Integer.parseInt(value));
                    strength = Integer.parseInt(value);
                }
            }
        });

        setSpinnerAlpha(correctionStrength);
        correctionStrength.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getChildAt(0) != null)
                    ((TextView) adapterView.getChildAt(0)).setTextColor(ContextCompat.getColor(requireContext(), R.color.colorS4HDarkBlue));
                model.getSelectedDeviceAgent().getSettingsManager().set("vibration-strength", String.valueOf((correctionStrength.getSelectedItemPosition())));
                strength = (correctionStrength.getSelectedItemPosition());
                setSetting(model.getSelectedDeviceAgent());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // Perform Calibration (Trigger)
    }

    public static Fragment newInstance() {
        return new UprightGo2ConfigurationFragment();
    }

    private void setSetting(Agent agent) {
        //some value adaptation
        int time = 5;
        if (interval == 0)
            time = 5;
        else if (interval == 1)
            time = 15;
        else if (interval == 2)
            time = 30;
        else if (interval == 3)
            time = 60;

        //Send Message vibration settings
        if (agent.getState() == Agent.AGENT_STATE_ENABLED) {

            UprightGo2VibrationProtocol vibrationProtocol;
            vibrationProtocol = new UprightGo2VibrationProtocol((UprightGo2Agent) agent, vibration, angle, interval, false, pattern, strength);
            vibrationProtocol.saveSettings();
        }
    }

    private void setSpinnerAlpha(Spinner spinner) {
        if (model.getSelectedDeviceAgent().getState() != Agent.AGENT_STATE_ENABLED) {
            spinner.setAlpha(0.5f);
        }
    }

    private static void setView(ViewGroup layout, boolean enabled) {
        setChildrenEnabled(layout, enabled);
    }

    private static void setChildrenEnabled(ViewGroup layout, boolean state) {
        layout.setEnabled(state);

        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                if (!layout.isEnabled()) {
                    layout.setAlpha(0.5f);
                } else layout.setAlpha(1);
                setChildrenEnabled((ViewGroup) child, state);
            } else {
                child.setEnabled(state);
                if (!child.isEnabled()) {
                    child.setAlpha(0.5f);
                } else child.setAlpha(1);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        vibration = postureCorrectionVibration.isChecked();
        model.getSelectedDeviceAgent().addStateObserver(agentStateObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (model.getSelectedDeviceAgent() != null) {
            model.getSelectedDeviceAgent().removeStateObserver(agentStateObserver);
        }
    }
}
