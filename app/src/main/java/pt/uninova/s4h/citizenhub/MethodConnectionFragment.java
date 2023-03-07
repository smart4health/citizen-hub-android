package pt.uninova.s4h.citizenhub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodConnectionFragment extends Fragment {

    List<String> connectionMethodsAvailable = new ArrayList<>(Arrays.asList("Bluetooth", "WearOS"));

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View result = inflater.inflate(R.layout.fragment_device_connection_method_list, container, false);

        ListView listConnectionMethods = result.findViewById(R.id.listViewConnectionMethods);
        ArrayList<String> connectionMethods = new ArrayList<>(connectionMethodsAvailable);
        MethodConnectionAdapter adapter = new MethodConnectionAdapter(requireContext(), connectionMethods);
        listConnectionMethods.setAdapter(adapter);
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
