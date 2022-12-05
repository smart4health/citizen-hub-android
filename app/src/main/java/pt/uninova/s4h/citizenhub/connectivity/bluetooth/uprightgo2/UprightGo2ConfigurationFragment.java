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
import androidx.navigation.Navigation;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.connectivity.Agent;
import pt.uninova.s4h.citizenhub.connectivity.StateChangedMessage;
import pt.uninova.s4h.citizenhub.connectivity.bluetooth.AbstractConfigurationFragment;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class UprightGo2ConfigurationFragment extends AbstractConfigurationFragment {

    public static String uprightGo2MenuItem = "calibration";
    private final Agent agent;

    private SwitchCompat postureCorrectionVibration;
    private LinearLayout buttonCalibration;
    private LinearLayout spinnerIntervalLayout;
    private LinearLayout spinnerPatternLayout;
    private LinearLayout spinnerStrengthLayout;
    private LinearLayout spinnerAngleLayout;
    Spinner correctionStrength;
    Spinner spinnerAngle;
    Spinner spinnerInterval;
    Spinner spinnerPattern;

    protected ViewStub deviceAdvancedSettings;
    protected View deviceAdvancedSettingsInflated;
    private boolean vibration;
    private int angle;
    private int interval;
    private int pattern;
    private boolean showPattern;
    private int strength;

    private final Observer<StateChangedMessage<Integer, ? extends Agent>> agentStateObserver = value -> {
        boolean state = value.getNewState() == Agent.AGENT_STATE_ENABLED;
        requireActivity().runOnUiThread(() -> {
            setView(spinnerAngleLayout, state);
            setView(buttonCalibration, state);
            setView(spinnerIntervalLayout, state);
            setView(spinnerStrengthLayout, state);
            setView(spinnerPatternLayout, state);

        });
    };

    private final Observer<String> postureSwitchObserver = new Observer<String>() {
        @Override
        public void observe(String value) {
            if (value.equals("true")) {
                vibration = true;
                requireActivity().runOnUiThread(() -> {
                    setView(buttonCalibration, true);
                    setView(spinnerIntervalLayout, true);
                    setView(spinnerStrengthLayout, true);
                    setView(spinnerPatternLayout, true);
                    postureCorrectionVibration.setChecked(true);

                });
            } else {
                if (value.equals("false")) {
                    vibration = false;
                    requireActivity().runOnUiThread(() -> {
                        setView(buttonCalibration, false);
                        setView(spinnerIntervalLayout, false);
                        setView(spinnerStrengthLayout, false);
                        setView(spinnerPatternLayout, false);
                        postureCorrectionVibration.setChecked(false);

                    });
                }
            }
        }
    };

    public UprightGo2ConfigurationFragment(Agent agent) {
        this.agent = agent;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_device_configuration_advanced, container, false);
        deviceAdvancedSettings = view.findViewById(R.id.layoutStubConfigurationAdvancedSettings);
        deviceAdvancedSettings.setLayoutResource(R.layout.fragment_device_configuration_uprightgo2);
        deviceAdvancedSettingsInflated = deviceAdvancedSettings.inflate();

        buttonCalibration = deviceAdvancedSettingsInflated.findViewById(R.id.calibrationLayout);
        postureCorrectionVibration = deviceAdvancedSettingsInflated.findViewById(R.id.switchPostureCorrection);
        spinnerIntervalLayout = deviceAdvancedSettingsInflated.findViewById(R.id.layoutVibrationInterval);
        spinnerPatternLayout = deviceAdvancedSettingsInflated.findViewById(R.id.layoutVibrationPattern);
        spinnerStrengthLayout = deviceAdvancedSettingsInflated.findViewById(R.id.layoutVibrationStrength);
        spinnerAngleLayout = deviceAdvancedSettingsInflated.findViewById(R.id.layoutVibrationAngle);
        spinnerAngle = deviceAdvancedSettingsInflated.findViewById(R.id.spinnerVibrationAngle);
        spinnerPattern = deviceAdvancedSettingsInflated.findViewById(R.id.spinnerVibrationPattern);
        spinnerInterval = deviceAdvancedSettingsInflated.findViewById(R.id.spinnerVibrationInterval);
        correctionStrength = deviceAdvancedSettingsInflated.findViewById(R.id.spinnerVibrationStrength);
        disableListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        agent.getSettingsManager().get("First Time", new Observer<String>() {
            @Override
            public void observe(String value) {
                if (value == null) {
                    agent.getSettingsManager().set("posture-correction-vibration", "false");
                    agent.getSettingsManager().set("vibration-angle", "0");
                    agent.getSettingsManager().set("vibration-interval", "0");
                    agent.getSettingsManager().set("vibration-pattern", "0");
                    agent.getSettingsManager().set("show-vibration-pattern", "false");
                    agent.getSettingsManager().set("vibration-strength", "0");

                    agent.getSettingsManager().set("First Time", "1");

                }
                setupAdvancedConfigurationsUprightGo2(deviceAdvancedSettingsInflated);
            }
        });

    }

    protected void setupAdvancedConfigurationsUprightGo2(View view) {
        //vibration-angle (1 (strict) to 6 (relaxed))

        agent.getSettingsManager().get("vibration-angle", new Observer<String>() {
            @Override
            public void observe(String value) {
                spinnerAngle.setSelection(Integer.parseInt(value));
                angle = Integer.parseInt(value);
            }
        });


        agent.getSettingsManager().get("vibration-interval", new Observer<String>() {
            @Override
            public void observe(String value) {
                spinnerInterval.setSelection(Integer.parseInt(value));
                interval = Integer.parseInt(value);
            }
        });

        //vibration-pattern (0 (long), 1 (medium), 2 (short), 3 (ramp up), 4 (knock knock),
        // 5 (heartbeat), 6 (tuk tuk), 7 (ecstatic), 8 (muzzle))

        agent.getSettingsManager().get("vibration-pattern", new Observer<String>() {
            @Override
            public void observe(String value) {
                spinnerPattern.setSelection(Integer.parseInt(value));
                pattern = Integer.parseInt(value);
            }
        });

        agent.getSettingsManager().get("vibration-strength", new Observer<String>() {
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
        setListeners();
        agent.getSettingsManager().get("posture-correction-vibration", postureSwitchObserver);

    }

    private void setListeners() {
        disableListeners();
        buttonCalibration.setOnClickListener(view1 -> Navigation.findNavController(requireView()).navigate(pt.uninova.s4h.citizenhub.ui.devices.DeviceConfigurationFragmentDirections.actionDeviceConfigurationStreamsFragmentToUprightGo2CalibrationFragment()));

        correctionStrength.post(new Runnable() {
            @Override
            public void run() {
                correctionStrength.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (adapterView.getChildAt(0) != null)
                            ((TextView) adapterView.getChildAt(0)).setTextColor(ContextCompat.getColor(requireContext(), R.color.colorS4HDarkBlue));
                        agent.getSettingsManager().set("vibration-strength", String.valueOf((correctionStrength.getSelectedItemPosition())));
                        strength = (correctionStrength.getSelectedItemPosition());
                        setSetting(agent, true);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        setSetting(agent, false);

                    }
                });
            }
        });

        spinnerPattern.post(new Runnable() {
            @Override
            public void run() {
                spinnerPattern.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (adapterView.getChildAt(0) != null)
                            ((TextView) adapterView.getChildAt(0)).setTextColor(ContextCompat.getColor(requireContext(), R.color.colorS4HDarkBlue));

                        agent.getSettingsManager().set("vibration-pattern", String.valueOf(spinnerPattern.getSelectedItemPosition()));
                        pattern = spinnerPattern.getSelectedItemPosition();
                        setSetting(agent, true);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        setSetting(agent, false);
                    }
                });
            }
        });

        spinnerInterval.post(new Runnable() {
            @Override
            public void run() {
                spinnerInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (adapterView.getChildAt(0) != null)
                            ((TextView) adapterView.getChildAt(0)).setTextColor(ContextCompat.getColor(requireContext(), R.color.colorS4HDarkBlue));
                        agent.getSettingsManager().set("vibration-interval", String.valueOf(spinnerInterval.getSelectedItemPosition()));
                        interval = spinnerInterval.getSelectedItemPosition();
                        setSetting(agent, false);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        setSetting(agent, false);
                    }
                });
            }
        });

        spinnerAngle.post(new Runnable() {
            @Override
            public void run() {
                spinnerAngle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (adapterView.getChildAt(0) != null) {
                            ((TextView) adapterView.getChildAt(0)).setTextColor(ContextCompat.getColor(requireContext(), R.color.colorS4HDarkBlue));
                        }
                        agent.getSettingsManager().set("vibration-angle", String.valueOf(spinnerAngle.getSelectedItemPosition()));
                        angle = spinnerAngle.getSelectedItemPosition();

                        setSetting(agent, false);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        setSetting(agent, false);
                    }
                });
            }
        });


    }

    private void setSetting(Agent agent, boolean vibrate) {
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
            vibrationProtocol = new UprightGo2VibrationProtocol((UprightGo2Agent) agent, vibration, angle, interval, vibrate, pattern, strength);
            vibrationProtocol.saveSettings();
        }

    }

    private void setSpinnerAlpha(Spinner spinner) {
        if (agent.getState() != Agent.AGENT_STATE_ENABLED) {
            spinner.setAlpha(0.5f);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        agent.addStateObserver(agentStateObserver);
        postureCorrectionVibration.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                agent.getSettingsManager().set("posture-correction-vibration", "true");
                vibration = true;
                setView(buttonCalibration, true);
                setView(spinnerIntervalLayout, true);
                setView(spinnerStrengthLayout, true);
                setView(spinnerPatternLayout, true);
                setSetting(agent, true);
            } else {
                agent.getSettingsManager().set("posture-correction-vibration", "false");
                vibration = false;
                setView(buttonCalibration, false);
                setView(spinnerIntervalLayout, false);
                setView(spinnerStrengthLayout, false);
                setView(spinnerPatternLayout, false);
                setSetting(agent, false);
            }
        });
        setSetting(agent, false);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (agent != null) {
            agent.removeStateObserver(agentStateObserver);
        }
    }

    private void disableListeners() {
        buttonCalibration.setOnClickListener(null);
        postureCorrectionVibration.setOnCheckedChangeListener(null);
        spinnerAngle.setOnItemSelectedListener(null);
        spinnerPattern.setOnItemSelectedListener(null);
        spinnerInterval.setOnItemSelectedListener(null);
        correctionStrength.setOnItemSelectedListener(null);
    }
}
