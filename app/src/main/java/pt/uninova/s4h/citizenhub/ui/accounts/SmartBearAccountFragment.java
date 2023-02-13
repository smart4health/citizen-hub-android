package pt.uninova.s4h.citizenhub.ui.accounts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.WorkManager;

import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.work.WorkOrchestrator;

public class SmartBearAccountFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.smart_bear_account_fragment, container, false);
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.smart_bear_account_fragment, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.smart_bear_account_fragment_menu_advanced) {
                    Navigation.findNavController(requireView()).navigate(SmartBearAccountFragmentDirections.actionSmartBearAccountFragmentToAdvancedSmartBearAccountGateFragment());
                    return true;
                } else if (menuItem.getItemId() == R.id.smart_bear_account_fragment_menu_log_out) {
                    final AccountsViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountsViewModel.class);
                    viewModel.disableSmartBearAccount();

                    WorkOrchestrator workOrchestrator = new WorkOrchestrator(WorkManager.getInstance(requireContext()));
                    workOrchestrator.cancelSmartBearUploader();

                    Navigation.findNavController(requireView()).navigate(SmartBearAccountFragmentDirections.actionSmartBearAccountFragmentToAccountsFragment());
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);


        return view;
    }
}
