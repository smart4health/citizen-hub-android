package pt.uninova.s4h.citizenhub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

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
        heartIcon = view.findViewById(R.id.imageIconHeartRate);

        initialAnimation();
        enableObservers();

        return view;
    }

    private void enableObservers() {
        MainActivity.listenHeartRateAverage.observe((LifecycleOwner) view.getContext(), s -> heartRateDataTextView.setText(s));
        MainActivity.listenSteps.observe((LifecycleOwner) view.getContext(), s -> stepsDataTextView.setText(s));
        MainActivity.protocolHeartRate.observe((LifecycleOwner) view.getContext(), aBoolean -> {
            if (aBoolean) {
                heartRateDataLayout.setVisibility(View.VISIBLE);
            } else {
                heartRateDataLayout.setVisibility(View.INVISIBLE);
            }
        });
        MainActivity.protocolSteps.observe((LifecycleOwner) view.getContext(), aBoolean -> {
            if (aBoolean) {
                stepsDataLayout.setVisibility(View.VISIBLE);
            } else {
                stepsDataLayout.setVisibility(View.INVISIBLE);
            }
        });
        MainActivity.heartRateIcon.observe((LifecycleOwner) view.getContext(), s -> heartIcon.setImageResource(s));
    }

    private void initialAnimation() {
        ImageView imageCitizen = view.findViewById(R.id.imageViewCitizenHub);

        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        fadeIn.setStartOffset(0);
        AlphaAnimation fadeInImage = new AlphaAnimation(0.0f, 1.0f);
        fadeInImage.setDuration(2500);
        fadeInImage.setStartOffset(0);
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(1000);
        fadeOut.setStartOffset(0);

        imageCitizen.startAnimation(fadeInImage);
    }
}