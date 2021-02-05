package pt.uninova.s4h.citizenhub.ui.lobby;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.io.File;
import java.io.IOException;

import pt.uninova.s4h.citizenhub.R;

public class PresentationFragment extends Fragment {
    private Button skipButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View result = inflater.inflate(R.layout.fragment_presentation, container, false);
        skipButton = result.findViewById(R.id.presentation_fragment_skip_button);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final File file = new File(requireContext().getFilesDir(), "first.txt");

                boolean fileCreated = false;

                try {
                    fileCreated = file.createNewFile();
                } catch (IOException ioe) {
                    System.out.println("Error while creating empty file: " + ioe);
                }

                if (fileCreated) {
                    System.out.println("Created empty file: " + file.getPath());
                } else {
                    System.out.println("Failed to create empty file: " + file.getPath());
                }
                Navigation.findNavController(requireView()).navigate(PresentationFragmentDirections.actionPresentationFragmentToAuthenticationFragment());
            }
        });
        return result;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
