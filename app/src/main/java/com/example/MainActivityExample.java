package com.example;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pt.uninova.s4h.citizenhub.R;
import pt.uninova.s4h.citizenhub.persistence.CitizenHubDatabase;
import pt.uninova.s4h.citizenhub.persistence.Device;
import pt.uninova.s4h.citizenhub.SharedDeviceViewModel;

import java.util.List;


public class MainActivityExample extends AppCompatActivity {
    public static final int ADD_DEVICE_REQUEST = 37;
    private SharedDeviceViewModel sharedDeviceViewModel;
    private CitizenHubDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.example_activity_main);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        // FloatingActionButton buttonAddDevice = findViewById(R.id.button_add_device);
        Button buttonAddDevice = findViewById(R.id.addz);
        buttonAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//

                recyclerView.setVisibility(View.INVISIBLE);

                Fragment currentFragment = AddDeviceFragment.getInstance();
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.addDeviceFragment, currentFragment)
                        .addToBackStack("")
                        .commit();
            }
        });
        db = CitizenHubDatabase.getInstance(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        DeviceAdapter adapter = new DeviceAdapter(R.layout.example_device_item);
        recyclerView.setAdapter(adapter);

        sharedDeviceViewModel = new ViewModelProvider(this).get(SharedDeviceViewModel.class);
        sharedDeviceViewModel.getAllDevicesLive().observe(this, new Observer<List<Device>>() {
            @Override
            public void onChanged(@Nullable List<Device> devices) {
                adapter.setDevices(devices);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            super.onBackPressed();
        }
    }


}

