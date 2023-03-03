package pt.uninova.s4h.citizenhub.connectivity.wearos;

import java.util.Date;

public class BaseChannelListener implements ChannelListener {

    final private int name;

    public BaseChannelListener(int name) {
        this.name = name;
    }

    @Override
    public int getChannelName() {
        return name;
    }

    @Override
    public void onChange(double value, Date timestamp) {}
    @Override
    public void onChange(double value, Date timestamp, long wear_sample_id) {}

    @Override
    public void onWrite(byte[] value) {}
}
