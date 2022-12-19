package pt.uninova.s4h.citizenhub.wearbasic;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import pt.uninova.s4h.citizenhub.R;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}