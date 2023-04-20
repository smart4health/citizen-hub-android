package pt.uninova.s4h.citizenhub;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

public class CurrentDateDecorator implements DayViewDecorator {

    private Drawable highlightDrawable;
    private Context context;

    public CurrentDateDecorator(Context context) {
        this.context = context;
        highlightDrawable = this.context.getResources().getDrawable(R.drawable.decorator_circle_background);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return day.equals(CalendarDay.today());
    }

    @Override
    public void decorate(DayViewFacade view) {

        view.setBackgroundDrawable(highlightDrawable);
        view.addSpan(new ForegroundColorSpan(Color.WHITE));
    }
}
