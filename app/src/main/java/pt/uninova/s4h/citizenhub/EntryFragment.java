package pt.uninova.s4h.citizenhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.io.File;

import care.data4life.sdk.Data4LifeClient;
import care.data4life.sdk.lang.D4LException;
import care.data4life.sdk.listener.ResultListener;

public class EntryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.entry_page_fragment, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onEntry();
    }

    public boolean firstInteraction() {
        final File file = new File(requireContext().getFilesDir(), "first.txt");
        return !file.exists();
    }

    public void onEntry() {

        if (firstInteraction()) {
            Navigation.findNavController(requireView()).navigate(EntryFragmentDirections.actionEntryPageFragmentToPresentationFragment());
        } else {
            Navigation.findNavController(requireView()).navigate(EntryFragmentDirections.actionEntryPageFragmentToAuthenticationFragment());
        }
    }

}