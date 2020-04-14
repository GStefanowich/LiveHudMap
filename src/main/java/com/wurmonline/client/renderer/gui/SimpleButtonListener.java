/*
 * This software is licensed under the MIT License
 * https://github.com/GStefanowich/LiveHudMap
 *
 * Copyright (c) 2019 Gregory Stefanowich
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
