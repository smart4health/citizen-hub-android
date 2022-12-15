package pt.uninova.s4h.citizenhub.wearbasic;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import pt.uninova.s4h.citizenhub.R;

public class DataDisplayFragment extends Fragment {

    LinearLayout heartRateDataLayout, stepsDataLayout;
    TextView heartRateDataTextView, stepsDataTextView, textBelow;
    View view;
    ImageView heartIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(
                R.layout.fragment_main, container, false);

        heartRateDataLayout = view.findViewById(R.id.heartRateDataLayout);
        heartRateDataTextView = view.findViewById(R.id.textViewHeartRateValue);
        stepsDataLayout = view.findViewById(R.id.stepsDataLayout);
        stepsDataTextView = view.findViewById(R.id.textViewStepsValue);
        textBelow = view.findViewById(R.id.textViewInitializing);
        heartIcon = view.findViewById(R.id.imageIconHeartRate);

        enableObservers();

        /*
        new CountDownTimer(10000,1000){
            @Override
            public void onTick(long millisecondsUntilDone) {}
            @Override
            public void onFinish() {
                textBelow.setVisibility(View.INVISIBLE);
            }
        }.start();
        */

        return view;
    }

    private void enableObservers(){
        MainActivity.listenHeartRateAverage.observe((LifecycleOwner) view.getContext(), s ->
        {
            heartRateDataTextView.setText(s);
            textBelow.setVisibility(View.INVISIBLE);
        }
        );
        MainActivity.listenSteps.observe((LifecycleOwner) view.getContext(), s -> stepsDataTextView.setText(s));
        MainActivity.heartRateIcon.observe((LifecycleOwner) view.getContext(), s -> heartIcon.setImageResource(s));
    }
}