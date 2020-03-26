package org.gotti.wurmonline.clientmods.livehudmap.reflection;

import com.wurmonline.client.renderer.GroundItemData;
import com.wurmonline.client.renderer.cell.GroundItemCellRenderable;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.lang.reflect.Field;

public final class GroundItems {
    private GroundItems() {}
    
    public static GroundItemData getData(GroundItemCellRenderable cell) {
        if (cell == null) return null;
        try {
            Field field = ReflectionUtil.getField(cell.getClass(), "item");
            return ReflectionUtil.getPrivateField(cell, field);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            LiveHudMapMod.log( e );
            return null;
        }
    }
    
}
