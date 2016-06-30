package de.epiceric.shopchest.language;

import org.bukkit.entity.EntityType;

public class EntityName {

    private String localizedName;
    private EntityType entityType;

    public EntityName(EntityType entityType, String localizedName) {
        this.entityType = entityType;
        this.localizedName = localizedName;
    }

    /**
     * @return EntityType linked to the name
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * @return Name linked to the EntityType
     */
    public String getLocalizedName() {
        return localizedName;
    }
}
