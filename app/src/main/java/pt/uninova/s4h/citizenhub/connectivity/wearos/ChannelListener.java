package pt.uninova.s4h.citizenhub.connectivity.wearos;

import java.util.Date;

public interface ChannelListener {
    int getChannelName();

    void onChange(double value, Date timestamp);

    void onChange(double value, Date timestamp, long wear_sample_id);

    void onWrite(byte[] value);
}
