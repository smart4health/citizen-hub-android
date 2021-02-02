package pt.uninova.s4h.citizenhub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.util.Objects;

public class PresentationFragment extends Fragment {

    private Button skipButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View result = inflater.inflate(R.layout.fragment_presentation, container, false);
        skipButton = result.findViewById(R.id.button_add_device);

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Navigation.findNavController(requireView()).navigate(PresentationFragmentDirections.actionPresentationFragmentToSummaryFragment());
            }
        });

        return result;
    }



}
