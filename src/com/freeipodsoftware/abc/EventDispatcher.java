package com.freeipodsoftware.abc;

import java.util.HashSet;
import java.util.Set;

public class EventDispatcher {
    private final Set<EventListener> listenerSet = new HashSet<>();

    public EventDispatcher() {
    }

    public void addListener(EventListener listener) {
        this.listenerSet.add(listener);
    }

    public void raiseEvent(String eventId) {
        if (eventId != null) {

            for (EventListener listener : this.listenerSet) {
                listener.onEvent(eventId);
            }
        }

    }
}
