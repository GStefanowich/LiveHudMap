package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.mesh.Tiles.Tile;

public class MapRendererTopographic extends AbstractSurfaceRenderer {
	private short interval;

	public MapRendererTopographic(NearTerrainDataBuffer buffer) {
		super(buffer);
		this.interval = 250;
	}

	@Override
	public BufferedImage createMapDump(int leftX, int topY, int winWidth, int winHeight, int playerX, int playerY) {
		if (topY < 0)
			topY = 0;
		if (leftX < 0)
			leftX = 0;

		final BufferedImage bi2 = new BufferedImage(winWidth, winWidth, BufferedImage.TYPE_INT_RGB);
		final float[] data = new float[winWidth * winWidth * 3];

		for (int x = 0; x < winWidth; x++) {
			for (int y = winWidth - 1; y >= 0; y--) {
				final short height = getSurfaceHeight(x + leftX, y + topY);
				final short nearHeightNX = x == 0 ? height : getSurfaceHeight(x + leftX - 1, y + topY);
				final short nearHeightNY = y == 0 ? height : getSurfaceHeight(x + leftX, y + topY - 1);
				final short nearHeightX = x == winWidth - 1 ? height : getSurfaceHeight(x + leftX + 1, y + topY);
				final short nearHeightY = y == winWidth - 1 ? height : getSurfaceHeight(x + leftX, y + topY + 1);
				boolean isControur = checkContourLine(height, nearHeightNX, interval)
						|| checkContourLine(height, nearHeightNY, interval)
						|| checkContourLine(height, nearHeightX, interval)
						|| checkContourLine(height, nearHeightY, interval);

				final Tile tile = getTileType(x + leftX, y + topY);
				final Color color;
				if (tile != null) {
					color = tile.getColor();
				}
				else {
					color = Tile.TILE_DIRT.getColor();
				}
				int r = color.getRed();
				int g = color.getGreen();
				int b = color.getBlue();
				if (isControur) {
					r = 0;
					g = 0;
					b = 0;
				}
				else if (height < 0) {
					r = (int) (r * 0.2f + 0.4f * 0.4f * 256f);
					g = (int) (g * 0.2f + 0.5f * 0.4f * 256f);
					b = (int) (b * 0.2f + 1.0f * 0.4f * 256f);
				}

				if (playerX == x + leftX && playerY == y + topY) {
					r = Color.RED.getRed();
					g = 0;
					b = 0;
				}

				data[(x + y * winWidth) * 3 + 0] = r;
				data[(x + y * winWidth) * 3 + 1] = g;
				data[(x + y * winWidth) * 3 + 2] = b;
			}
		}

		bi2.getRaster().setPixels(0, 0, winWidth, winWidth, data);
		return bi2;
	}

	private boolean checkContourLine(short h0, short h1, short interval) {
		if (h0 == h1) {
			return false;
		}
		for (int i = h0; i <= h1; i++) {
			if (i % interval == 0) {
				return true;
			}
		}
		return false;
	}

}
