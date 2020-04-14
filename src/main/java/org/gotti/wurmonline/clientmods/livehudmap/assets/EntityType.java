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

import com.wurmonline.client.util.SecureStrings;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;

import java.awt.Color;

public enum EntityType {
    // PLAYER
    PLAYER(EntityFilter.PLAYER, Color.RED),
    TRADER(EntityFilter.CREATURE, Color.WHITE),
    OBJECT(EntityFilter.OBJECTS, Color.WHITE),
    // VEHICLES
    WAGON(EntityFilter.VEHICLE, new Color( 150, 75, 0 )),
    CART(EntityFilter.VEHICLE, new Color( 150, 75, 0 )),
    SHIP(EntityFilter.VEHICLE, new Color( 150, 75, 0 )),
    // ANIMALS
    COW(EntityFilter.CREATURE, Color.WHITE),
    CHICKEN(EntityFilter.CREATURE, Color.WHITE),
    PIG(EntityFilter.CREATURE, Color.PINK),
    SHEEP(EntityFilter.CREATURE, Color.WHITE),
    BISON(EntityFilter.CREATURE, new Color(84, 42, 0 )),
    HORSE(EntityFilter.CREATURE, new Color(111, 55, 0 )),
    CRAB(EntityFilter.HOSTILE, new Color(240,128,128)),
    UNICORN(EntityFilter.HOSTILE, Color.WHITE),
    CYCLOPS(EntityFilter.HOSTILE),
    GOBLIN(EntityFilter.HOSTILE),
    SPIDER(EntityFilter.HOSTILE),
    // GENERIC ANIMALS
    HELL_MONSTER(EntityFilter.HOSTILE, new Color( 220,20,60 )),
    SEA_CREATURE(EntityFilter.CREATURE, Color.BLUE),
    SEA_MONSTER(EntityFilter.HOSTILE, Color.BLUE),
    REPTILE_MONSTER(EntityFilter.HOSTILE, new Color(46,139,87)),
    RIFT_MONSTER(EntityFilter.HOSTILE, new Color(178,34,34)),
    // RARE CREATURES
    DRAKE(EntityFilter.HOSTILE, Color.CYAN),
    DRAGON(EntityFilter.HOSTILE, Color.CYAN),
    TROLL_KING(EntityFilter.HOSTILE),
    // NPC-like
    SPIRIT_GUARD(EntityFilter.CREATURE, Color.WHITE),
    TOWER_GUARD(EntityFilter.CREATURE, Color.WHITE),
    GUARD(EntityFilter.CREATURE, Color.WHITE),
    GUARD_TOWER(EntityFilter.OBJECTS, Color.DARK_GRAY),
    HOLIDAY(EntityFilter.CREATURE, Color.GREEN);
    
    private final Color color;
    private final EntityFilter parent;
    
    EntityType() {
        this(EntityFilter.CREATURE);
    }
    EntityType(EntityFilter parent) {
        this(parent, Color.RED);
    }
    EntityType(EntityFilter parent, Color color) {
        this.parent = parent;
        this.color = color;
    }
    
    public EntityFilter getParent() {
        return this.parent;
    }
    public Color getColor() {
        return this.color;
    }
    
    public static EntityType getByModelName(SecureStrings secure) {
        String model = secure.toString();
        if (model.startsWith("model.furniture") || model.startsWith("model.decoration"))
            return null;
        switch ( model ) {
            case "model.creature.drake.black":
            case "model.creature.drake.green":
            case "model.creature.drake.white":
            case "model.creature.drake.blue":
            case "model.creature.drake.red":
                return DRAKE;
            case "model.creature.dragon.red":
            case "model.creature.dragon.blue":
            case "model.creature.dragon.green":
            case "model.creature.dragon.black":
            case "model.creature.dragon.white":
                return DRAGON;
            case "model.creature.humanoid.lavacreature":
            case "model.creature.multiped.spider.lava":
            case "model.creature.multiped.scorpion.hell":
                return HELL_MONSTER;
            case "model.creature.fish.dolphin":
            case "model.creature.fish.blue.whale":
            case "model.creature.fish.blue.octopus":
            case "model.creature.fish.seal":
                return SEA_CREATURE;
            case "model.creature.snake.serpent.sea":
            case "model.creature.fish.shark.huge":
                return SEA_MONSTER;
            case "model.creature.quadraped.crab":
                return CRAB;
            case "model.creature.humanoid.human.guard":
                return GUARD;
            default: {
                if (model.startsWith("model.container.spiritcastle") || model.startsWith("model.container.spiritmansion") || model.startsWith("model.decoration.altar") || model.startsWith("model.board.village") || model.startsWith("model.sign.messageboard")|| model.startsWith("model.sign.recruitment"))
                    return OBJECT;
                if (model.startsWith("model.structure.guardtower") || model.startsWith("model.structure.neutraltower"))
                    return GUARD_TOWER;
                if (model.startsWith("model.creature.quadraped.sheep") || model.startsWith("model.creature.quadraped.lamb"))
                    return SHEEP;
                if (model.startsWith("model.creature.quadraped.pig"))
                    return PIG;
                if (model.startsWith("model.creature.humanoid.hen") || model.startsWith("model.creature.humanoid.rooster") || model.startsWith("model.creature.humanoid.chicken"))
                    return CHICKEN;
                if (model.startsWith("model.creature.quadraped.bison"))
                    return BISON;
                if (model.startsWith("model.creature.humanoid.human.guard"))
                    return GUARD;
                if (model.startsWith("model.creature.humanoid.human.player"))
                    return PLAYER;
                if (model.startsWith("model.creature.humanoid.human.salesman"))
                    return TRADER;
                if (model.startsWith("model.creature.quadraped.horse.hell") || model.startsWith("model.creature.quadraped.dog.hell"))
                    return HELL_MONSTER;
                if (model.startsWith("model.creature.quadraped.horse"))
                    return HORSE;
                if (model.startsWith("model.creature.quadraped.unicorn"))
                    return UNICORN;
                if (model.startsWith("model.transports.medium.wagon"))
                    return WAGON;
                if (model.startsWith("model.structure.large.cart") || model.startsWith("model.structure.small.cart") || model.startsWith("model.transports"))
                    return CART;
                if (model.startsWith("model.structure.boat") || model.startsWith("model.structure.small.boat"))
                    return SHIP;
            }
            //LiveHudMapMod.log( "Unknown map entity model: "+ model );
        }
        return null;
    }
    
}
