package pt.uninova.s4h.citizenhub.ui.devices;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import pt.uninova.s4h.citizenhub.R;

public class DeviceConfigurationUdiSetterFragment extends Fragment {
    private DeviceViewModel model;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_device_configuration_unique_identifier_setter, container, false);
        Button setUdi = view.findViewById(R.id.udi_set_button);
        Button clearUdi = view.findViewById(R.id.udi_clear_button);
        EditText udiText = view.findViewById(R.id.fragment_device_configuration_udi_setter_textview);

        udiText.setOnClickListener(view1 -> {
            udiText.requestFocus();

            InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(view1, InputMethodManager.SHOW_IMPLICIT);
        });

        udiText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                setUdi.setEnabled(s.toString().length() > 0);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        setUdi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model.getSelectedDeviceAgent().getSettingsManager().set("uid", udiText.getText().toString());
                Navigation.findNavController(DeviceConfigurationUdiSetterFragment.this.requireView()).navigate(DeviceConfigurationUdiSetterFragmentDirections.actionDeviceConfigurationUdiSetterFragmentToDeviceConfigurationFragment());

            }
        });

        clearUdi.setOnClickListener(view12 -> {
            Navigation.findNavController(DeviceConfigurationUdiSetterFragment.this.requireView()).navigate(DeviceConfigurationUdiSetterFragmentDirections.actionDeviceConfigurationUdiSetterFragmentToDeviceConfigurationFragment());
            model.getSelectedDeviceAgent().getSettingsManager().set("uid", "None");
        });

        return view;
    }

}
