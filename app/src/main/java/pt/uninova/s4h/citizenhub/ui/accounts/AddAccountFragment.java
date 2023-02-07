package pt.uninova.s4h.citizenhub.ui.accounts;

import static care.data4life.sdk.Data4LifeClient.D4L_AUTH;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.WorkManager;

import care.data4life.sdk.Data4LifeClient;
import care.data4life.sdk.lang.D4LException;
import care.data4life.sdk.listener.ResultListener;
import pt.uninova.s4h.citizenhub.MainActivity;
import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.work.WorkOrchestrator;

public class AddAccountFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.accounts_fragment, container, false);

        AccountsViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountsViewModel.class);

        View smart4HealthCard = view.findViewById(R.id.card_smart4health);
        View smartBearCard = view.findViewById(R.id.card_smartbear);

        smart4HealthCard.setVisibility(!viewModel.hasSmart4HealthAccount() ? View.VISIBLE : View.GONE);
        smartBearCard.setVisibility(!viewModel.hasSmartBearAccount() ? View.VISIBLE : View.GONE);

        smart4HealthCard.setOnClickListener((View v) -> {

            final Intent loginIntent = Data4LifeClient.getInstance().getLoginIntent(requireActivity(), null);
            startActivityForResult(loginIntent, D4L_AUTH);

        });
        smartBearCard.setOnClickListener((View v) -> {
            Navigation.findNavController(requireView()).navigate(AddAccountFragmentDirections.actionAddAccountFragmentToAdvancedSmartBearAccountGateFragment());
        });

        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == D4L_AUTH) {
            authenticate();
        }
    }
    private void authenticate() {
        final Data4LifeClient client = Data4LifeClient.getInstance();

        client.isUserLoggedIn(new ResultListener<Boolean>() {
            @Override
            public void onSuccess(Boolean value) {
                if (value) {
                    final Activity activity = requireActivity();
                    final Intent intent = new Intent(activity, MainActivity.class);

                    AccountsViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountsViewModel.class);
                    viewModel.enableSmartHealthAccount();

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intent);
                    activity.finish();

                    WorkOrchestrator workOrchestrator = new WorkOrchestrator(WorkManager.getInstance(requireContext()));
                    workOrchestrator.enqueueSmart4HealthUploader();
                    workOrchestrator.enqueueSmart4HealthWeeklyUploader();
                    workOrchestrator.enqueueSmart4HealthMonthlyUploader();
                }

            }

            @Override
            public void onError(D4LException exception) {
                exception.printStackTrace();
            }
        });
    }
}
