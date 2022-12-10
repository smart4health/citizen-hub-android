package pt.uninova.s4h.citizenhub.wearbasic;

import android.os.Bundle;
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
    TextView heartRateDataTextView, stepsDataTextView, swipeLeft;
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
        swipeLeft = view.findViewById(R.id.textViewSwipe);
        heartIcon = view.findViewById(R.id.imageIconHeartRate);

        enableObservers();

        return view;
    }

    private void enableObservers(){
        MainActivity.listenHeartRateAverage.observe((LifecycleOwner) view.getContext(), s -> heartRateDataTextView.setText(s));
        MainActivity.listenSteps.observe((LifecycleOwner) view.getContext(), s -> stepsDataTextView.setText(s));
        MainActivity.protocolHeartRate.observe((LifecycleOwner) view.getContext(), aBoolean -> {
            if (aBoolean) {
                heartRateDataLayout.setVisibility(View.VISIBLE);
                swipeLeft.setVisibility(View.GONE);
            } else {
                heartRateDataLayout.setVisibility(View.INVISIBLE);
            }
        });
        MainActivity.protocolSteps.observe((LifecycleOwner) view.getContext(), aBoolean -> {
            if (aBoolean) {
                stepsDataLayout.setVisibility(View.VISIBLE);
                swipeLeft.setVisibility(View.GONE);
            } else {
                stepsDataLayout.setVisibility(View.INVISIBLE);
            }
        });
        MainActivity.heartRateIcon.observe((LifecycleOwner) view.getContext(), s -> heartIcon.setImageResource(s));
    }
}