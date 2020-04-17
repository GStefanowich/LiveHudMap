package org.gotti.wurmonline.clientmods.livehudmap.reflection;

import com.wurmonline.client.renderer.cell.Cell;
import com.wurmonline.client.renderer.cell.CellRenderable;

import java.lang.reflect.Field;
import java.util.List;

public final class Entities {
    private Entities() {}
    
    private static final Field MOBILE_RENDERABLE = Reflection.getField(Cell.class, "mobileRenderables");
    private static final Field STATIC_RENDERABLE = Reflection.getField(Cell.class, "staticRenderables");
    
    public static List<CellRenderable> getEntities(Cell cell) {
        return Reflection.getPrivateField(cell, MOBILE_RENDERABLE);
    }
    public static List<CellRenderable> getGroundItems(Cell cell) {
        return Reflection.getPrivateField(cell, STATIC_RENDERABLE);
    }
    
}
