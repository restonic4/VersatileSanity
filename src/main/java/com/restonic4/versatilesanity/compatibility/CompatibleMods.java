package com.restonic4.versatilesanity.compatibility;

public enum CompatibleMods {
    TOUGH_AS_NAILS("toughasnails");

    private final String id;

    CompatibleMods(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}