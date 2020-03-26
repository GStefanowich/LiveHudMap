package org.gotti.wurmonline.clientmods.livehudmap.assets;

public enum Kingdoms {
    JENN_KELLON,
    MOL_REHAN,
    HORDE_OF_THE_SUMMONED,
    FREEDOM;
    
    public static Kingdoms getById(int id) {
        switch (id) {
            case 1: return JENN_KELLON;
            case 2: return MOL_REHAN;
            case 3: return HORDE_OF_THE_SUMMONED;
            case 4: return FREEDOM;
        }
        return null;
    }
    
}
