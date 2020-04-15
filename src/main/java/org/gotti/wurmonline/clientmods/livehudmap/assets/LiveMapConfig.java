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

package org.gotti.wurmonline.clientmods.livehudmap.assets;

import java.util.Properties;
import java.util.function.Function;

public class LiveMapConfig {
    private LiveMapConfig() {}
    
    public static boolean HIGH_RES_MAP = false;
    public static boolean SHOW_HIDDEN_ORE = false;
    
    public static int MAP_TILE_SIZE = 64;
    public static int THREAD_COUNT = 2;
    public static int SAVE_SECONDS = 30;
    
    /**
     * @param properties List of properties
     * @param key The key of the Property to read
     * @param parse The Function for converting the String value to the desired Object
     * @return The parsed value
     */
    public static <T> T parse(Properties properties, String key, Function<String, T> parse) {
        String stringVal = properties.getProperty(key);
        try {
            return parse.apply(stringVal);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException( "Incorrect value \"" + stringVal + "\" for Config " + key, e );
        }
    }
    
    /**
     * @param integer The string to parse
     * @return The int value of the String
     * @throws NumberFormatException If the String is not a Number
     */
    public static int parseInt(String integer) throws NumberFormatException {
        return Integer.parseInt(integer);
    }
    
    /**
     * @param bool The string to parse
     * @return The bool value of the String
     * @throws IllegalArgumentException If the String is not a Boolean
     */
    public static boolean parseBoolean(String bool) throws IllegalArgumentException {
        if (!( "true".equalsIgnoreCase(bool) || "false".equalsIgnoreCase(bool) ))
            throw new IllegalArgumentException("Invalid Boolean Value \"" + bool + "\"");
        return Boolean.parseBoolean(bool);
    }
}
