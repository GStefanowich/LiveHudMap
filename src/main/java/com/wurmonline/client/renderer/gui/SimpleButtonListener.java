package com.wurmonline.client.renderer.gui;

public interface SimpleButtonListener extends ButtonListener {
    
    @Override
    default void buttonPressed(WButton b) {}
    
    static WurmPopup.WPopupAbstractButton livePopup( WurmPopup popup, String text, final Runnable supplier ) {
        return popup.new WPopupLiveButton( text ) {
            @Override
            protected void handleLeftClick() {
                supplier.run();
            }
        };
    }
    static WurmPopup.WPopupAbstractButton deadPopup( WurmPopup popup, String text ) {
        return popup.new WPopupDeadButton( text, null );
    }
    static String toggleHidden(boolean shown) {
        return shown ? "Hide" : "Show";
    }
    static String toggleEnable(boolean enabled) {
        return enabled ? "Disable" : "Enable";
    }
    
}
