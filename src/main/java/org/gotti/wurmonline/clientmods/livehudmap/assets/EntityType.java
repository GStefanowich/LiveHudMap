package org.gotti.wurmonline.clientmods.livehudmap.assets;

import com.wurmonline.client.util.SecureStrings;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;

import java.awt.Color;

public enum EntityType {
    // PLAYER
    PLAYER(EntityFilter.PLAYER, Color.RED),
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
        String modelName = secure.toString();
        switch ( modelName ) {
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
            case "model.creature.quadraped.dog.hell":
            case "model.creature.multiped.scorpion.hell":
                return HELL_MONSTER;
            case "model.creature.quadraped.lamb":
            case "model.creature.quadraped.sheep":
                return SHEEP;
            case "model.creature.quadraped.pig":
                return PIG;
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
            case "model.creature.humanoid.chicken":
            case "model.creature.humanoid.hen":
            case "model.creature.humanoid.rooster":
                return CHICKEN;
            case "model.creature.humanoid.human.guard":
                return GUARD;
            default: {
                if (modelName.startsWith("model.creature.quadraped.horse.hell"))
                    return HELL_MONSTER;
                if (modelName.startsWith("model.creature.quadraped.horse"))
                    return HORSE;
                if (modelName.startsWith("model.transports.medium.wagon"))
                    return WAGON;
                if (modelName.startsWith("model.structure.large.cart") || modelName.startsWith("model.structure.small.cart"))
                    return CART;
                if (modelName.startsWith("model.structure.boat") || modelName.startsWith("model.structure.small.boat"))
                    return SHIP;
            }
        }
        return null;
    }
    
}
