package com.spaxxv.cardsmp.recipes;

public class PatternGroup implements Matchable {
    private PatternItem[] items;
    public PatternGroup(PatternItem ...i) {
        items = i;
    }
    private boolean matches(PatternItem patternItem) {
        for (PatternItem item : items) {
            if (item.matches(patternItem)) return true;
        }
        return false;
    }
    private boolean matches(PatternGroup patternGroup) {
        if (patternGroup.items.length != items.length) return false;
        for (int i = 0; i < items.length; i++) {
            if (!patternGroup.items[i].matches(items[i])) return false;
        }
        return true;
    }
    @Override
    public boolean matches(Matchable matchable) {
        if (matchable instanceof PatternItem i) {
            return matches(i);
        } else if (matchable instanceof PatternGroup g) {
            return matches(g);
        }
        return false;
    }
}
