package de.epiceric.shopchest.interfaces;

import org.bukkit.entity.EntityType;

public abstract class SpawnEggMeta {

    /**
     * @return The NBT Tag <i>EntityTag.id</i> of the Spawn Egg
     */
    public abstract String getNBTEntityID();

    /**
     * @param nbtEntityID EntityID returned by {@link #getNBTEntityID()}
     * @return The {@link EntityType} the Spawn Egg will spawn or <b>null</b> if <i>nbtEntityID</i> is null
     */
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

