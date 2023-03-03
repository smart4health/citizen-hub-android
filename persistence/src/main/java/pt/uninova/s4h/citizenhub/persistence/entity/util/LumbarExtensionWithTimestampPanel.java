package pt.uninova.s4h.citizenhub.persistence.entity.util;

import java.time.Instant;

public class LumbarExtensionWithTimestampPanel extends AbstractLumbarExtensionPanel {

    private Instant timestamp;

    public LumbarExtensionWithTimestampPanel(Double duration, Double repetitions, Double score, Double weight, Instant timestamp){
        super(duration, repetitions, score, weight);
        this.timestamp = timestamp;
    }

    public String getTimestamp(){return timestamp.toString();}

    public void setTimestamp(Instant timestamp){this.timestamp = timestamp;}

}
