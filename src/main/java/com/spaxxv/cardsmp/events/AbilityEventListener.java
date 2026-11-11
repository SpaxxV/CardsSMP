package com.spaxxv.cardsmp.events;

import com.spaxxv.cardsmp.events.AbilityEvent;

@FunctionalInterface
public interface AbilityEventListener {
    void onFire(AbilityEvent event);
}
