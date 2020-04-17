package org.gotti.wurmonline.clientmods.livehudmap.assets;

import java.awt.image.BufferedImage;

public enum TileRenderLayer {
    TERRAIN {
        @Override
        public int getImageType() {
            return BufferedImage.TYPE_INT_RGB;
        }
    },
    ENTITY {
        @Override
        public int getImageType() {
            return BufferedImage.TYPE_INT_ARGB;
        }
    };
    
    public final int numValues() {
        return this.getImageType() == BufferedImage.TYPE_INT_ARGB ? 4 : 3;
    }
    public abstract int getImageType();
}
