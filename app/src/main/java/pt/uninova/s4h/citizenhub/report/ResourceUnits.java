package pt.uninova.s4h.citizenhub.report;

public class ResourceUnits implements LocalizedResource {

    private final String units;

    public ResourceUnits(String units){
        this.units = units;
    }

    @Override
    public String getLocalizedString() {
        return units;
    }

}
