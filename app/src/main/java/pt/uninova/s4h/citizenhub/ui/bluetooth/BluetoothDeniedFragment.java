package pt.uninova.s4h.citizenhub.ui.bluetooth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import pt.uninova.s4h.citizenhub.R;

public class BluetoothDeniedFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View result = inflater.inflate(R.layout.fragment_bluetooth_denied, container, false);
        return result;

    }

}

