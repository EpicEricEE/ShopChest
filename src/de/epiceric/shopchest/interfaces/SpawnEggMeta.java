package de.epiceric.shopchest.interfaces;

import org.bukkit.entity.EntityType;

public abstract class SpawnEggMeta {

    public abstract String getNBTEntityID();

    public EntityType getEntityTypeFromNBTEntityID(String nbtEntityID) {
        if (nbtEntityID == null) return null;

        switch (nbtEntityID) {
            case "PigZombie":
                return EntityType.PIG_ZOMBIE;
            case "CaveSpider":
                return EntityType.CAVE_SPIDER;
            case "LavaSlime":
                return EntityType.MAGMA_CUBE;
            case "MushroomCow":
                return EntityType.MUSHROOM_COW;
            case "EntityHorse":
                return EntityType.HORSE;
            case "PolarBear":
                return EntityType.POLAR_BEAR;
            default:
                return EntityType.valueOf(nbtEntityID.toUpperCase());

        }
    }

}

