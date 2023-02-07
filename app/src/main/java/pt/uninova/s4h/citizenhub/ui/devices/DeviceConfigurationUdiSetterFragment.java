package pt.uninova.s4h.citizenhub.ui.devices;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

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
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.device_configuration_udi_setter_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.device_configuration_udi_clear_item) {

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            model.getSelectedDeviceAgent().getSettingsManager().set("udi-serial-number", null);
                            model.getSelectedDeviceAgent().getSettingsManager().set("udi-system", null);
                            model.getSelectedDeviceAgent().getSettingsManager().set("udi-device-identifier", null);

                        }
                    });
                    Navigation.findNavController(DeviceConfigurationUdiSetterFragment.this.requireView()).navigate(DeviceConfigurationUdiSetterFragmentDirections.actionDeviceConfigurationUdiSetterFragmentToDeviceConfigurationFragment());
                    return true;
                }
                return false;
            }
        });
        LinearLayout serialNumber = view.findViewById(R.id.layout_serial_number);
        LinearLayout deviceIdentifierLayout = view.findViewById(R.id.layout_device_identifier);
        TextView serialNumberPlaceholder = view.findViewById(R.id.value_placeholder_serial_number);
        TextView deviceIdentifierPlaceholder = view.findViewById(R.id.value_placeholder_device_identifier);
        Spinner systemSpinner = view.findViewById(R.id.spinner_udi_system);

        model.getSelectedDeviceAgent().getSettingsManager().get("udi-serial-number", new Observer<String>() {
            @Override
            public void observe(String value) {
                if (value != null) {
                    serialNumberPlaceholder.setText(value);
                }
            }
        });

        model.getSelectedDeviceAgent().getSettingsManager().get("udi-device-identifier", new Observer<String>() {
            @Override
            public void observe(String value) {
                if (value != null) {
                    deviceIdentifierPlaceholder.setText(value);
                }
            }
        });

        deviceIdentifierLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.UdiAlertDialogStyle);
                builder.setTitle(getString(R.string.label_device_identifier));
                final EditText input = new EditText(requireContext());

                Drawable wrappedDrawable = DrawableCompat.wrap(input.getBackground());
                DrawableCompat.setTint(wrappedDrawable.mutate(), getResources().getColor(R.color.colorS4HDarkBlue));
                DrawableCompat.setTint(wrappedDrawable.mutate(), getResources().getColor(R.color.colorS4HDarkBlue));
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(deviceIdentifierPlaceholder.getText());
                builder.setView(input);
                input.requestFocus();
                showKeyboard();

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeKeyboard();
                        deviceIdentifierPlaceholder.setText(input.getText().toString());
                        model.getSelectedDeviceAgent().getSettingsManager().set("udi-device-identifier", input.getText().toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeKeyboard();
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        serialNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.UdiAlertDialogStyle);
                builder.setTitle(getString(R.string.label_serial_number));
                final EditText input = new EditText(requireContext());

                Drawable wrappedDrawable = DrawableCompat.wrap(input.getBackground());
                DrawableCompat.setTint(wrappedDrawable.mutate(), getResources().getColor(R.color.colorS4HDarkBlue));
                DrawableCompat.setTint(wrappedDrawable.mutate(), getResources().getColor(R.color.colorS4HDarkBlue));

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(serialNumberPlaceholder.getText());

                builder.setView(input);
                input.requestFocus();
                showKeyboard();

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeKeyboard();
                        serialNumberPlaceholder.setText(input.getText().toString());
                        model.getSelectedDeviceAgent().getSettingsManager().set("udi-serial-number", input.getText().toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeKeyboard();
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });


        model.getSelectedDeviceAgent().getSettingsManager().get("udi-system", new Observer<String>() {
            @Override
            public void observe(String value) {
                if (value != null) {
                    systemSpinner.setSelection(Integer.parseInt(value));
                }
            }
        });
        systemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getChildAt(0) != null) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(ContextCompat.getColor(requireContext(), R.color.colorS4HDarkBlue));
                }
                model.getSelectedDeviceAgent().getSettingsManager().set("udi-system", String.valueOf(systemSpinner.getSelectedItemPosition()));

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return view;
    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

}
