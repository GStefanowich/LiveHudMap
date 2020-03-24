package com.wurmonline.client.renderer.gui;

import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;

import com.wurmonline.client.renderer.backend.Queue;

public class LiveMapView extends FlexComponent {
	private final LiveMap liveMap;
	
	LiveMapView(String name, LiveMap liveMap, int width, int height) {
		super( name );
		this.setInitialSize( width, height, false );
		this.sizeFlags = FlexComponent.FIXED_WIDTH | FlexComponent.FIXED_HEIGHT;
		
		this.liveMap = liveMap;
	}
	
	@Override
	protected void renderComponent(Queue queue, float alpha) {
		super.renderComponent(queue, alpha);
		
		this.liveMap.update( this.x, this.y );
		this.liveMap.render(queue, 1.0F);
	}
	
	@Override
	public void mouseWheeled(int mouseX, int mouseY, int delta) {
		if (delta > 0) liveMap.zoomOut();
		else liveMap.zoomIn();
	}
	
}
