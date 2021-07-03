package pt.uninova.s4h.citizenhub;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import pt.uninova.s4h.citizenhub.connectivity.AgentOrchestrator;
import pt.uninova.s4h.citizenhub.service.CitizenHubServiceBound;

public class DeviceConfigurationAddFragment extends DeviceConfigurationFragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_device_configuration_add, container, false);
        final DeviceViewModel model = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
        ProgressBar progressBar = view.findViewById(R.id.add_pprogressBar);
        connectDevice = view.findViewById(R.id.buttonConfiguration);
        setupViews(view);
        setupText();
        loadFeatureState();
        connectDevice.setOnClickListener(v -> {
            AgentOrchestrator agentOrchestrator = ((CitizenHubServiceBound) requireActivity()).getService().getAgentOrchestrator();
            agentOrchestrator.addDeviceToMap(model.getSelectedDevice().getValue());
            model.apply();
            progressBar.setVisibility(View.VISIBLE);
            connectDevice.setText("Please wait...");
            //   setFeaturesState(model.getSelectedAgent(requireActivity()));
            saveFeaturesChosen();
//            ProgressButton progressButton = new ProgressButton(getContext(), v);
//            progressButton.StartProgress();
            Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Navigation.findNavController(requireView()).navigate(DeviceConfigurationAddFragmentDirections.actionDeviceConfigurationAddFragmentToDeviceListFragment());

                }
            }, 10000);
        });

        return view;
    }


}
