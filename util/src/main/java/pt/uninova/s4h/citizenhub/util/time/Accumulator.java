package pt.uninova.s4h.citizenhub.util.time;

import java.time.Duration;
import java.time.Instant;

import pt.uninova.s4h.citizenhub.util.Pair;
import pt.uninova.s4h.citizenhub.util.messaging.Dispatcher;
import pt.uninova.s4h.citizenhub.util.messaging.Observer;

public class Accumulator<T> {

    private Instant timestamp;
    private T value;

    private final Dispatcher<Pair<T, Duration>> dispatcher;

    private boolean notify;

    public Accumulator() {
        this.dispatcher = new Dispatcher<>();

        this.timestamp = null;
        this.value = null;

        this.notify = false;
    }

    public void addObserver(Observer<Pair<T, Duration>> observer) {
        dispatcher.addObserver(observer);
    }

    public void clear() {
        dispatcher.close();
    }

    public void flush() {
        flush(Instant.now());
    }

    public void flush(Instant timestamp) {
        if (this.timestamp != null) {
            notify(timestamp);

            this.timestamp = timestamp;
        }
    }

    public boolean isRunning() {
        return this.timestamp != null;
    }

    private void notify(Instant timestamp) {
        dispatcher.dispatch(new Pair<>(value, Duration.between(this.timestamp, timestamp)));
    }

    public void removeObserver(Observer<Pair<T, Duration>> observer) {
        dispatcher.removeObserver(observer);
    }

    public void set(T value) {
        set(value, Instant.now());
    }

    public void set(T value, Instant timestamp) {
        if (this.timestamp == null) {
            notify = true;
        } else if (notify || !this.value.equals(value)) {
            notify(timestamp);
        }

        this.timestamp = timestamp;
        this.value = value;
    }

    public void stop() {
        stop(Instant.now());
    }

    public void stop(Instant timestamp) {
        if (this.timestamp != null) {
            notify(timestamp);
        }

        this.value = null;
        this.timestamp = null;
    }
}
