package pt.uninova.s4h.citizenhub.data;

public class Tag {

    public static final int LABEL_CONTEXT_WORK = 1;

    public static final int LABEL_BODY_POSITION_SITTING = 2;

    public static final int LABEL_MEASUREMENT_NOT_SYNCHRONIZED = 3;

    public static final int LABEL_MEASUREMENT_SYNCHRONIZED = 4;

    private final int label;

    public Tag(int label) {
        this.label = label;
    }

    public int getLabel() {
        return label;
    }
}
