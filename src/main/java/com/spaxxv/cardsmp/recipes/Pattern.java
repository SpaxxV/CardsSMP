package com.spaxxv.cardsmp.recipes;

import com.spaxxv.cardsmp.CardSMP;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class Pattern {
    public enum Type {
        CRAFTING, CRAFTING_SHAPELESS
    }
    public static PatternBuilder builder() {
        return builder(Type.CRAFTING);
    }
    public static PatternBuilder builder(@NotNull Type type) {
        switch (type) {
            case CRAFTING -> {
                return new PatternBuilder(new Pattern(9, false));
            }
            case CRAFTING_SHAPELESS -> {
                return new PatternBuilder(new Pattern(9, true));
            }
        }
        return new PatternBuilder(new Pattern(9, false));
    }
    private Matchable[] patternItems;
    private final ArrayList<Matchable> arrayList;
    private final boolean isDynamic;
    private int sizeLimit;
    private boolean compiled;
    private Pattern(int size, boolean dynamic) {
        isDynamic = dynamic;
        sizeLimit = size;
        compiled = false;
        if (isDynamic) {
            patternItems = null;
            arrayList = new ArrayList<>();
        } else {
            arrayList = null;
            patternItems = new Matchable[size];
        }
    }
    public int getSize() {
        return sizeLimit;
    }
    public boolean isCompiled() {
        return compiled;
    }
    public void setItem(int index, Matchable item) throws IllegalAccessException, IndexOutOfBoundsException {
        if (compiled) throw new IllegalAccessException("Cannot edit a compiled pattern!");
        if (index < 0 || index >= sizeLimit) throw new IndexOutOfBoundsException("Index " + index + " is out of size limit " + sizeLimit + " for pattern.");
        if (isDynamic) {
            arrayList.add(index, item);
        } else {
            patternItems[index] = item;
        }
    }
    public void compile() throws IllegalAccessException {
        if (compiled) throw new IllegalAccessException("Cannot compile an already compiled pattern!");
        if (isDynamic) {
            patternItems = new Matchable[arrayList.size()];
            for (int i = 0; i < patternItems.length; i++) {
                patternItems[i] = arrayList.get(i);
            }
        }
        sizeLimit = patternItems.length;
        compiled = true;
    }
    public boolean matches(Pattern pattern) {
        if (!compiled || !pattern.compiled) return false;
        if (isDynamic) {
            Matchable[] firstItemList = patternItems.clone();
            Matchable[] secondItemList = pattern.patternItems.clone();
            for (int i = 0; i < sizeLimit; i++) {
                Matchable currentItem = firstItemList[i];
                if (currentItem == null) continue;
                for (int n = 0; n < pattern.sizeLimit; n++) {
                    if (secondItemList[n] == null) continue;
                    if (secondItemList[n].matches(currentItem)) {
                        secondItemList[n] = null;
                        firstItemList[i] = null;
                        break;
                    }
                }
            }
            for (int j = 0; j < sizeLimit; j++) {
                if (firstItemList[j] != null) return false;
            }
            for (int k = 0; k < pattern.sizeLimit; k++) {
                if (secondItemList[k] != null) return false;
            }
        } else {
            if (pattern.sizeLimit != sizeLimit) return false;
            for (int i = 0; i < sizeLimit; i++) {
                Matchable currentItem = patternItems[i];
                if (pattern.patternItems[i] != null && currentItem != null) {
                    if (!currentItem.matches(pattern.patternItems[i])) return false;
                } else if (!(pattern.patternItems[i] == null && currentItem == null)) return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Pattern" + Arrays.toString(patternItems);
    }
}
