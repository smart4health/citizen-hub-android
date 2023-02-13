package pt.uninova.s4h.citizenhub.report;

public class ResourceType implements LocalizedResource {

    private final String type;

    public ResourceType(String type){
        this.type = type;
    }

    @Override
    public String getLocalizedString() {
        return type;
    }

}
