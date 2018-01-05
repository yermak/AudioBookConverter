package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.StateListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Yermak on 03-Jan-18.
 */
public class StateDispatcher implements StateListener {
    private final Set<StateListener> listenerSet = new HashSet<>();
    private static final Map<String, StateDispatcher> instances = new HashMap();

    public static synchronized StateDispatcher getInstance() {
        return getInstance("");
    }

    public static synchronized StateDispatcher getInstance(String name) {
        return instances.computeIfAbsent(name, s -> new StateDispatcher());
    }

    private StateDispatcher() {
    }

    public void addListener(StateListener listener) {
        this.listenerSet.add(listener);
    }

    @Override
    public void finishedWithError(String error) {
        listenerSet.forEach(l -> {
            l.finishedWithError(error);
        });
    }

    @Override
    public void finished() {
        listenerSet.forEach(l -> {
            l.finished();
        });
    }

    @Override
    public void canceled() {
        listenerSet.forEach(l -> {
            l.canceled();
        });
    }

    @Override
    public void paused() {
        listenerSet.forEach(l -> {
            l.paused();
        });
    }

    @Override
    public void resumed() {
        listenerSet.forEach(l -> {
            l.resumed();
        });
    }

    public void fileListChanged() {
        listenerSet.forEach(l -> {
            l.fileListChanged();
        });
    }

    @Override
    public void modeChanged(ConversionMode mode) {
        listenerSet.forEach(l -> {
            l.modeChanged(mode);
        });
    }
}
