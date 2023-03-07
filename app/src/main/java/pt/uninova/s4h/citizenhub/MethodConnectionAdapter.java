package pt.uninova.s4h.citizenhub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import java.util.ArrayList;

public class MethodConnectionAdapter extends ArrayAdapter<String> {

    String bluetoothConnectionMethod = "Bluetooth", wearConnectionMethod = "WearOS";

    public MethodConnectionAdapter(@NonNull Context context, ArrayList<String> methods) {
        super(context, 0, methods);
    }

    private int getImageResource(String name){
        if(name.equals(bluetoothConnectionMethod))
            return R.drawable.ic_bluetooth;
        else if (name.equals(wearConnectionMethod))
            return R.drawable.ic_wearos;
        else
            return 0;
    }

    private void setClickListener(String name, View convertView){
        if(name.equals(bluetoothConnectionMethod))
            convertView.setOnClickListener(view -> Navigation.findNavController(convertView)
                    .navigate(MethodConnectionFragmentDirections.actionDeviceConnectionMethodFragmentToDeviceSearchFragment()));
        else if (name.equals(wearConnectionMethod))
            convertView.setOnClickListener(view -> Navigation.findNavController(convertView)
                    .navigate(MethodConnectionFragmentDirections.actionDeviceConnectionMethodFragmentToDeviceSearchWearosFragment()));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        String name = getItem(position);
        if (convertView==null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_device_connection_method_list_item, parent, false);

        ImageView methodImage = convertView.findViewById(R.id.image_method);
        TextView methodName = convertView.findViewById(R.id.text_view_title_method);
        methodImage.setImageResource(getImageResource(name));
        methodName.setText(name);
        setClickListener(name, convertView);

        return convertView;
    }
}
