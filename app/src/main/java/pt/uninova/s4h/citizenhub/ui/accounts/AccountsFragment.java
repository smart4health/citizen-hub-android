package pt.uninova.s4h.citizenhub.ui.accounts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import pt.uninova.s4h.citizenhub.R;

public class AccountsFragment extends Fragment {

    private AccountsViewModel viewModel;
    private ExtendedFloatingActionButton addButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.accounts_fragment, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(AccountsViewModel.class);
        addButton = view.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(requireView()).navigate(AccountsFragmentDirections.actionAccountsFragmentToAddAccountFragment());
            }
        });
        if (viewModel.hasSmart4HealthAccount() && viewModel.hasSmartBearAccount()) {
            addButton.setVisibility(View.GONE);
        } else {
            addButton.setVisibility(View.VISIBLE);
        }

        View smart4HealthCard = view.findViewById(R.id.card_smart4health);
        View smartBearCard = view.findViewById(R.id.card_smartbear);

        smart4HealthCard.setVisibility(viewModel.hasSmart4HealthAccount() ? View.VISIBLE : View.GONE);
        smartBearCard.setVisibility(viewModel.hasSmartBearAccount() ? View.VISIBLE : View.GONE);

        smart4HealthCard.setOnClickListener((View v) -> {
            NavController controller = Navigation.findNavController(requireView());
            addButton.setVisibility(View.GONE);
            controller.navigate(AccountsFragmentDirections.actionAccountsFragmentToSmart4healthAccountFragment());
        });

        smartBearCard.setOnClickListener((View v) -> {
            addButton.setVisibility(View.GONE);
            Navigation.findNavController(requireView()).navigate(AccountsFragmentDirections.actionAccountsFragmentToSmartBearAccountFragment());
        });

        return view;
    }
}