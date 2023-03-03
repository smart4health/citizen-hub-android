package pt.uninova.s4h.citizenhub.persistence.entity.util;

public class AbstractCaloriesPanel implements CaloriesPanel{

    private Double calories;

    protected AbstractCaloriesPanel (Double calories){
        this.calories = calories;
    }

    @Override
    public Double getCalories() {
        return calories;
    }

    @Override
    public void setCalories(Double value) {
        calories = value;
    }

}
