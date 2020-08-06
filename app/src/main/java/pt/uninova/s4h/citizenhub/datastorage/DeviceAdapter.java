package pt.uninova.s4h.citizenhub.datastorage;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pt.uninova.s4h.citizenhub.ui.R;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceHolder> {
    private List<Device> devices = new ArrayList<>();
    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_item, parent, false);
        return new DeviceHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull DeviceHolder holder, int position) {
        Device currentDevice = devices.get(position);
        holder.textViewTitle.setText(currentDevice.getName());
        holder.textViewDescription.setText(currentDevice.getAddress());
        holder.textViewPriority.setText(String.valueOf(currentDevice.getType()));
    }
    @Override
    public int getItemCount() {
        return devices.size();
    }
    public void setDevices(List<Device> devices) {
        this.devices = devices;
        notifyDataSetChanged();
    }
    class DeviceHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewDescription;
        private TextView textViewPriority;
        public DeviceHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewDescription = itemView.findViewById(R.id.text_view_description);
            textViewPriority = itemView.findViewById(R.id.text_device);
        }
    }
}
