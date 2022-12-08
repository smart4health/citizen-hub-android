package pt.uninova.s4h.citizenhub.wearbasic.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import pt.uninova.s4h.citizenhub.wearbasic.DataDisplayFragment;

public class ScreenSlidePagerAdapter extends FragmentStateAdapter {

    public ScreenSlidePagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new DataDisplayFragment();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
