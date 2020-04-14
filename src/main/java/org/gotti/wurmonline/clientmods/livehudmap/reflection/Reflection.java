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

package org.gotti.wurmonline.clientmods.livehudmap.reflection;

import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.lang.reflect.Field;

public final class Reflection {
    private Reflection() {}
    
    public static Field getField(Class<?> cl, String field) {
        try {
            return ReflectionUtil.getField(cl, field);
        } catch (NoSuchFieldException e) {
            LiveHudMapMod.log(e);
        }
        return null;
    }
    public static <T> T getPrivateField(Object object, Field field) {
        if (object == null) return null;
        try {
            return ReflectionUtil.getPrivateField(object, field);
        } catch (IllegalAccessException e) {
            LiveHudMapMod.log(e);
            return null;
        }
    }
    
}
