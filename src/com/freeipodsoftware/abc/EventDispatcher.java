package com.freeipodsoftware.abc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class EventDispatcher {
    private Set<EventListener> listenerSet = new HashSet();

    public EventDispatcher() {
    }

    public void addListener(EventListener listener) {
        this.listenerSet.add(listener);
    }

    public void removeListener(EventListener listener) {
        this.listenerSet.remove(listener);
    }

    public void raiseEvent(String eventId) {
        if (eventId != null) {
            Iterator var3 = this.listenerSet.iterator();

            while (var3.hasNext()) {
                EventListener listener = (EventListener) var3.next();
                listener.onEvent(eventId);
            }
        }

    }
}
