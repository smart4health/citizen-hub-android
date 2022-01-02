package pt.uninova.s4h.citizenhub.ui.accounts;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.time.LocalDateTime;

import pt.uninova.s4h.citizenhub.R;

public class AdvancedSmartBearAccountGateFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.advanced_smart_bear_account_gate_fragment, container, false);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.advanced_smart_bear_account_gate_fragment, menu);

        menu.findItem(R.id.advanced_smart_bear_account_gate_fragment_menu_confirm).setOnMenuItemClickListener(item -> {
            final EditText inputCodeEditText = requireView().findViewById(R.id.advanced_smart_bear_account_gate_fragment_input_code);

            if (validate(inputCodeEditText.getText().toString())) {
                Navigation.findNavController(requireView()).navigate(AdvancedSmartBearAccountGateFragmentDirections.actionAdvancedSmartBearAccountGateFragmentToAdvancedSmartBearAccountFragment());
            } else {
                inputCodeEditText.getText().clear();
            }

            return true;
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        View view = requireActivity().getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private boolean validate(String code) {
        try {
            int month = Integer.parseInt(code.substring(0, 2));
            int day = Integer.parseInt(code.substring(2, 4));
            int hour = Integer.parseInt(code.substring(4, 6));

            final LocalDateTime now = LocalDateTime.now();

            System.out.println(now.toString());
            return month == now.getMonthValue() && day == now.getDayOfMonth() && hour == now.getHour();
        } catch (NumberFormatException e) {
            return false;
        }
    }
}