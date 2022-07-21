package pt.uninova.s4h.citizenhub.report;

import java.text.DecimalFormat;

public class Item {

    private final LocalizedResource label;
    private final LocalizedResource value;
    private final LocalizedResource units;

    public Item(LocalizedResource label, LocalizedResource value, LocalizedResource units){
        this.label = label;
        this.value = value;
        this.units = units;
    }

    /***************************************
     * This section only has get functions *
     ***************************************/
    public LocalizedResource getLabel(){
        return label;
    }

    public LocalizedResource getValue(){
        return value;
    }

    public LocalizedResource getUnits() { return units; }

    public String getValueWithUnits(){
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        if(getUnits().getLocalizedString().equals("-"))
            return decimalFormat.format(Double.valueOf(getValue().getLocalizedString()));
        return (decimalFormat.format(Double.valueOf(getValue().getLocalizedString())) + " " + getUnits().getLocalizedString());
    }

}
